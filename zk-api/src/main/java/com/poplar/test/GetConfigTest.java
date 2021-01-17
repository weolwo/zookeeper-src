package com.poplar.test;

import com.poplar.bean.CustomizeConfigData;
import com.poplar.utils.ZookeeperInstance;
import com.poplar.watcher.ConfigWatcherAndCallBack;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Create BY poplar ON 2021/1/16
 * 模仿企业使用zk监听和获取某个配置信息,类似于实现自己的配置中心
 */
public class GetConfigTest {

    private ZooKeeper zk;

    @Before
    public void before() {
        zk = ZookeeperInstance.getZooKeeper("localhost:2181/testConfig");
    }

    @After
    public void after() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //真正处理业务的方法
    @Test
    public void getConfig() {

        ConfigWatcherAndCallBack callBack = new ConfigWatcherAndCallBack();
        CustomizeConfigData configData = new CustomizeConfigData();
        callBack.setConfigData(configData);
        callBack.setZooKeeper(zk);


        callBack.await();
        //存在2种情况：
        //1.一开始/AppConfig还不存在
        //2./AppConfig存在

        while (true) {
            if (configData.getData().equals("")) {
                System.out.println("configData 为空...");
                callBack.await();
            } else {
                System.out.println(configData.getData());
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
