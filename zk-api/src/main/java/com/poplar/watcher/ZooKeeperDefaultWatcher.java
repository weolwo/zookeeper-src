package com.poplar.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * Create BY poplar ON 2021/1/16
 * 第一类：new zk 时候，传入的watch，这个watch，是session级别的，跟path 、node没有关系。
 */
public class ZooKeeperDefaultWatcher implements Watcher {

    CountDownLatch countDownLatch;

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        Watcher.Event.KeeperState state = event.getState();
        Watcher.Event.EventType type = event.getType();
        String path = event.getPath();
        System.out.println("new zk " + event.toString());

        //根据不同的状态使用不同的回调
        switch (state) {
            case Unknown:
                break;
            case Disconnected:
                break;
            case NoSyncConnected:
                break;
            case SyncConnected:
                System.out.println("SyncConnected 同步链接成功....");
                countDownLatch.countDown();
                break;
            case AuthFailed:
                break;
            case ConnectedReadOnly:
                break;
            case SaslAuthenticated:
                break;
            case Expired:
                break;
            case Closed:
                break;
        }

        switch (type) {
            case None:
                break;
            case NodeCreated:
                System.out.println("create node");
                break;
            case NodeDeleted:
                break;
            case NodeDataChanged:
                System.out.println("NodeDataChanged");
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
}
