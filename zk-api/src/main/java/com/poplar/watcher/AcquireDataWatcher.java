package com.poplar.watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Create BY poplar ON 2021/1/16
 * 一个获取数据的观察者
 */
public class AcquireDataWatcher implements Watcher {

    ZooKeeper zk= null;
    Stat stat=null;

    public AcquireDataWatcher(ZooKeeper zk, Stat stat) {
        this.zk = zk;
        this.stat= stat;
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("getData watch: " + event.toString());
        try {
            //watcher为 true   调用的回调是创建zk对象时 default Watch  被重新注册   new zk的那个watch
            //zk.getData("/xxoo",true,stat);

            //如果需要继续监听当前这个watcher 重新注册即可
            zk.getData("/xxoo",this,stat);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
