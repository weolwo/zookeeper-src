package com.poplar.test;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BY Alex CREATED 2021/8/1
 * 监听servers下面节点的变化，客服端能够实时洞察到服务器上下线变化
 */


public class ZKClientTest {

    private final static String hostName = "192.168.19.100:2181";
    private final static int timeOut = 20000;
    ZooKeeper zooKeeper;

    public static void main(String[] args) throws Exception {
        ZKClientTest client = new ZKClientTest();
        //1.获取链接
        client.getConnection(hostName, timeOut);
        //2.获取服务列表
        client.getServerList();
        //3.处理业务
        client.handleBusiness();
    }

    private void handleBusiness() throws InterruptedException {
        Thread.sleep(Integer.MAX_VALUE);
    }

    private void getServerList() throws Exception {
        List<String> servers = new ArrayList<>();
        List<String> children = zooKeeper.getChildren("/servers", true);
        //获取到 /servers下面的所有节点
        for (String child : children) {
            //获取 /servers下面的所有节点的数据,不在需要监听节点下面数据的变化
            byte[] data = zooKeeper.getData("/servers/" + child, false, null);
            servers.add(new String(data));
        }
        System.out.println(servers);
    }

    private void getConnection(String hostName, int timeOut) throws Exception {

        zooKeeper = new ZooKeeper(hostName, timeOut, watchedEvent -> {
            //需要不断地监听节点的变化，而不是只监听一次,修改节点中的内容不会监听到（因为在处理服务列表的时候我们选择了不在监听子节点下面数据变化）
            try {
                getServerList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
