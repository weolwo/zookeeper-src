package com.poplar.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * BY Alex CREATED 2021/8/2
 * 分布式锁测试
 */


public class ZkDistributedLockTest {

    public static void main(String[] args) throws Exception {

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ZkDistributedLock distributedLock = new ZkDistributedLock();
            Thread thread = new Thread(() -> {
                try {
                    //获取锁
                    distributedLock.lock();
                    System.out.println(Thread.currentThread().getName() + "加锁成功");
                    TimeUnit.SECONDS.sleep(1);
                    //释放锁
                    distributedLock.unLock();
                    System.out.println(Thread.currentThread().getName() + "释放锁");
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
}
