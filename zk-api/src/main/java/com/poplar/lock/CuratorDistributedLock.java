package com.poplar.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BY Alex CREATED 2021/8/4
 * 使用 curator框架实现分布式锁
 */


public class CuratorDistributedLock {

    public static void main(String[] args) {

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            InterProcessMutex distributedLock = new InterProcessMutex(getCuratorFramework(), "/locks");
            Thread thread = new Thread(() -> {
                try {
                    //获取锁
                    distributedLock.acquire();
                    System.out.println(Thread.currentThread().getName() + "加锁成功");
                    distributedLock.acquire();
                    System.out.println(Thread.currentThread().getName() + "再次加锁成功");
                    TimeUnit.SECONDS.sleep(1);
                    //释放锁
                    distributedLock.release();
                    System.out.println(Thread.currentThread().getName() + "释放锁");
                    distributedLock.release();
                    System.out.println(Thread.currentThread().getName() + "再次释放锁");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            list.add(thread);
        }
        for (Thread thread : list) {
            thread.start();
        }
    }

    private static CuratorFramework getCuratorFramework() {
        //重试策略,3000后重试，并且可以重试三次
        ExponentialBackoffRetry policy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("192.168.19.100:2181").connectionTimeoutMs(6000)
                .sessionTimeoutMs(2000).retryPolicy(policy).build();

        client.start();
        return client;
    }
}
