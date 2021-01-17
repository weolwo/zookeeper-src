package com.poplar.test;

import com.poplar.utils.ZookeeperInstance;
import com.poplar.watcher.LockWatcherAndCallback;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Create BY poplar ON 2021/1/17
 * 使用zk去实现自己的分布式锁
 */
public class LockTest {

    private ZooKeeper zk;

    @Before
    public void before() {
        //后面可以直接跟一个路径
        zk = ZookeeperInstance.getZooKeeper("localhost:2181/testLock");
    }

    @After
    public void after() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        //模仿10个线程去抢锁
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                LockWatcherAndCallback lockWatcherAndCallback = new LockWatcherAndCallback();
                lockWatcherAndCallback.setThreadName(threadName);
                lockWatcherAndCallback.setZooKeeper(zk);
                //抢锁
                lockWatcherAndCallback.lock();
                //处理业务逻辑
                System.out.println(threadName + "  working ... ");
             /*   try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                //释放锁
                lockWatcherAndCallback.unlock();
            }).start();
        }

        while (true) {

        }
    }
}
