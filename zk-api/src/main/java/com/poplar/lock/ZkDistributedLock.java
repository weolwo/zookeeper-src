package com.poplar.lock;

import com.poplar.test.ZKClientTest;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * BY Alex CREATED 2021/8/2
 * zookeeper 分布式锁实现
 */


public class ZkDistributedLock {

    private final static String hostName = "192.168.19.100:2181";
    private final static int timeOut = 20000;
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final CountDownLatch watchedNodeLatch = new CountDownLatch(1);
    private final ZooKeeper zooKeeper;
    private String watchedNode;
    private String currentNode;

    public ZkDistributedLock() throws Exception {
        this.zooKeeper = new ZooKeeper(hostName, timeOut, watchedEvent -> {
            //判断zk是否已经连接成功了
            if (watchedEvent.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                countDownLatch.countDown();
            }
            //监听删除节点
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted && watchedEvent.getPath().equals(watchedNode)) {
                watchedNodeLatch.countDown();
            }
        });
        //等zk完全实例化
        countDownLatch.await();
        //判断是否有根节点
        Stat stat = zooKeeper.exists("/locks", false);
        if (stat == null) {
            zooKeeper.create("/locks", "locks".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

    public void lock() {
        try {
            //创建临时节点
            currentNode = zooKeeper.create("/locks/seq_", null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            //获取节点
            List<String> children = zooKeeper.getChildren("/locks", false);
            //如果只有一个，直接获取锁成功
            if (children.size() == 1) {
                return;
            }
            //排序
            Collections.sort(children);
            //seq_000000000
            String thisNode = currentNode.substring("/locks/".length());
            //判断自己在集合中的位置，如果自己是第一个，直接加锁成功，否则就需要监听自己前一个节点的状态
            int index = children.indexOf(thisNode);
            if (index < 0) {
                System.out.println("数据异常");
            } else if (index == 0) {
                //说明自己就是集合的最前面一个元素，加锁成功
                return;
            } else {
                //监听自己的前一个节点
                watchedNode = "/locks/" + children.get(index - 1);
                zooKeeper.getData(watchedNode, true, null);
                //等待上一个节点被释放
                watchedNodeLatch.await();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unLock() throws InterruptedException {
        try {
            zooKeeper.delete(currentNode, -1);
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

}
