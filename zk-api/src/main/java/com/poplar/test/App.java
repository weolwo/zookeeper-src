package com.poplar.test;

import com.poplar.utils.ZookeeperInstance;
import com.poplar.watcher.AcquireDataWatcher;
import com.poplar.watcher.ZooKeeperDefaultWatcher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Create BY poplar ON 2021/1/16
 * zk api编程测试
 * zk存储的数据也是二进制安全的和 redis 一样
 */
public class App {

    //zk是有session概念的，没有连接池的概念
    //watch:观察，回调
    //watch的注册值发生在 读类型调用，get，exites。。。
    //第一类：new zk 时候，传入的watch，这个watch，session级别的，跟path 、node没有关系。
    public static void main(String[] args) throws Exception {
        System.out.println("hello .....");
        //回调方法

        CountDownLatch cd = new CountDownLatch(1);
        ZooKeeperDefaultWatcher defaultWatcher = new ZooKeeperDefaultWatcher();
        defaultWatcher.setCountDownLatch(cd);
        ZooKeeper zk = ZookeeperInstance.getZooKeeper("127.0.0.1:2181");//new ZooKeeper("127.0.0.1:2181", 5000, defaultWatcher);

        //阻塞等待
        cd.await();
        ZooKeeper.States zkState = zk.getState();
        switch (zkState) {
            case CONNECTING:
                System.out.println("connecting");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("connected");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        String path = zk.create("/AppConfig", "hello".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(path);
        //节点的状态信息
        Stat stat = new Stat();
        //取数据的同时注册一个观察方法的回调，未来如果这个节点数据有变化的时候通知我
        byte[] zkData = zk.getData("/xxoo", new AcquireDataWatcher(zk, stat), stat);

        System.out.println("zkData: " + new String(zkData));

        //触发回调
        Stat stat1 = zk.setData("/xxoo", "newdata".getBytes(), 0);
        System.out.println("stat1");
        //还会触发吗？不会，watcher是一次性的，如果需要
        Stat stat2 = zk.setData("/xxoo", "newdata01".getBytes(), stat1.getVersion());

        System.out.println("-------async start----------");
        zk.getData("/xxoo", false, (rc, path1, ctx, data, stat3) -> {
            System.out.println("-------async call back----------");
            System.out.println(ctx.toString());
            System.out.println(new String(data));

        }, "abc");
        System.out.println("-------async over----------");

        TimeUnit.SECONDS.sleep(10);
    }
}
