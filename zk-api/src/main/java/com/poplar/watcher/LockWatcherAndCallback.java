package com.poplar.watcher;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Create BY poplar ON 2021/1/17
 */
public class LockWatcherAndCallback implements Watcher, AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback {

    private ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String threadName;

    private String pathName;

    //每个线程创建自己的序列
    public void lock() {
        try {
            zooKeeper.create("/lock", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "lock");
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        try {
            zooKeeper.delete(pathName, -1);
            System.out.println(threadName + " over work....");
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    //如果第一个哥们，那个锁释放了，其实只有第二个收到了回调事件！！
    //如果，不是第一个哥们，某一个，挂了，也能造成他后边的收到这个通知，从而让他后边那个跟去watch挂掉这个哥们前边的。。。
    //观察相关事件回调
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zooKeeper.getChildren("/", false, this, "lock");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }

    //节点抽奖将成功数据回调
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            System.out.println(threadName + " create node: " + name);//Thread-7 create node: /lock0000000000
            pathName = name;
            //这里watch为 false，主要是考虑监控父节点成本太高，只需要监控自己就ok
            zooKeeper.getChildren("/", false, this, "lock");
        }
    }

    //getChildren的callback
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        //一定要能看到自己前面的
       /* System.out.println(threadName+" look locks：");
        for (String child : children) {
            System.out.println(child);
        }*/

        //由于list集合中的元素是乱序的
        Collections.sort(children);
        //看自己是不是第一个
        int index = children.indexOf(pathName.substring(1));
        //yes
        if (index == 0) {
            System.out.println(threadName + " I am first ...");
            countDownLatch.countDown();
        } else {
            //no
            //监控自己前面的那一个
            zooKeeper.exists("/" + children.get(index - 1), this, this, "lock");
        }

    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }


    //watch exists的回调
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
