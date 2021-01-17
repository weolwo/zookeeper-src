package com.poplar.utils;

import com.poplar.watcher.ZooKeeperDefaultWatcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Create BY poplar ON 2021/1/16
 * 用于获取zk实例的
 */
public class ZookeeperInstance {

    private static final CountDownLatch init = new CountDownLatch(1);

    private static final ZooKeeperDefaultWatcher defaultWatcher = new ZooKeeperDefaultWatcher();

    private static ZooKeeper zooKeeper = null;

    public static ZooKeeper getZooKeeper(String address) {

        try {
            defaultWatcher.setCountDownLatch(init);
            zooKeeper = new ZooKeeper(address, 1000, defaultWatcher);
            //由于zk建立链接可能需要一段时间，为了避免拿到未完全初始化的对象
            init.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }
}
