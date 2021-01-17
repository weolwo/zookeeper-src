package com.poplar.watcher;

import com.poplar.bean.CustomizeConfigData;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;

/**
 * Create BY poplar ON 2021/1/16
 */
public class ConfigWatcherAndCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {

    private static ZooKeeper zooKeeper;

    private CustomizeConfigData configData;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    //监听节点数据变化
    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                zooKeeper.getData("/AppConfig", this, this, "getConfig");
                break;
            case NodeDeleted:
                //应该让获取数据的方法进入等待状态
                countDownLatch = new CountDownLatch(1);
                configData.setData("");
                break;
            case NodeDataChanged:
                zooKeeper.getData("/AppConfig", this, this, "getConfig");
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

    //状态回调，如果节点数据存在，stat就不为null
    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        if (stat != null) {
            zooKeeper.getData("/AppConfig", this, this, "getConfig");
        }
    }

    //数据回调，如果有数据data就不会为null
    @Override
    public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

        if (data != null) {
            try {
                //由于windos控制台使用的是gbk编码集
                configData.setData(new String(data, "GBK"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }
    }

    public void await() {
        try {
            zooKeeper.exists("/AppConfig", this, this, "getConfig");
            //进入阻塞
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public CustomizeConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(CustomizeConfigData configData) {
        this.configData = configData;
    }
}
