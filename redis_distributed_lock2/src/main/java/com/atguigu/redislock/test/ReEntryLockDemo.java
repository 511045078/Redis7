package com.atguigu.redislock.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Liu
 * @apiNote 测试
 * @date 2024/4/19 10:18
 */
public class ReEntryLockDemo
{

    final Object obj = new Object();

    /**
     * TODO 案例1
     *
     * @author Liu
     * @date 2024/4/19 10:27
     */
    public void entry01()
    {

        new Thread(() -> {
            synchronized (obj)
            {
                System.out.println(Thread.currentThread().getName() + "\t 外层调用");
                synchronized (obj)
                {
                    System.out.println(Thread.currentThread().getName() + "\t 中层调用");
                    synchronized (obj)
                    {
                        System.out.println(Thread.currentThread().getName() + "\t 内层调用");
                    }
                }
            }
        }, "t1").start();

    }

    /**
     * TODO 案例1
     *
     * @author Liu
     * @date 2024/4/19 10:27
     */
    private void entry02()
    {
        m1();
    }

    private synchronized void m1()
    {
        System.out.println(Thread.currentThread().getName() + "\t 外层调用synchronized");
        m2();
    }

    private synchronized void m2()
    {
        System.out.println(Thread.currentThread().getName() + "\t 中层调用synchronized");
        m3();
    }
    private synchronized void m3()
    {
        System.out.println(Thread.currentThread().getName() + "\t 内层调用synchronized");
    }

    Lock lock = new ReentrantLock();

    /**
     * TODO 案例3
     *
     * @author Liu
     * @date 2024/4/19 10:37
     */
    private void entry03()
    {
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "\t 外层调用lock");
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + "\t 内层调用lock");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    lock.unlock();
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }, "t1").start();

        // 暂停毫秒
        try { TimeUnit.MILLISECONDS.sleep(2); }catch (InterruptedException e){ e.printStackTrace(); }

        new Thread(() -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "\t 外层调用lock");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }, "t2").start();

    }

    public static void main(String[] args)
    {
        ReEntryLockDemo demo = new ReEntryLockDemo();
        // demo.entry01();
        // demo.entry02();
        demo.entry03();
    }
}
