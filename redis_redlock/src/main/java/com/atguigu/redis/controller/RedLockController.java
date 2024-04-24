package com.atguigu.redis.controller;

import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author Liu
 * @apiNote
 * @date 2024/4/22 17:12
 */

@RestController
@Slf4j
public class RedLockController
{
    public static final String CACHE_KEY_REDLOCK = "ATGUIGU_REDLOCK";

    @Autowired
    private RedissonClient redissonClient1;
    @Autowired
    private RedissonClient redissonClient2;
    @Autowired
    private RedissonClient redissonClient3;

    @GetMapping(value = "/multilock")
    public String getMultiLock(){
        String taskThreadID = Thread.currentThread().getId() + "";
        RLock lock1 = redissonClient1.getLock(CACHE_KEY_REDLOCK);
        RLock lock2 = redissonClient2.getLock(CACHE_KEY_REDLOCK);
        RLock lock3 = redissonClient3.getLock(CACHE_KEY_REDLOCK);
        RedissonMultiLock redLock = new RedissonMultiLock(lock1, lock2, lock3);
        redLock.lock();
        try {
            //业务
            log.info("come in biz multilock:{}" + taskThreadID);
            try { TimeUnit.SECONDS.sleep(30); } catch (InterruptedException e) { e.printStackTrace(); }
            log.info("task is over multilock:{}" + taskThreadID);
        }catch (Exception e){
            e.printStackTrace();
            log.error("multilock exception"+ CACHE_KEY_REDLOCK);
        }finally {
            redLock.unlock();
            log.info("释放分布式锁成功key：{}"+ CACHE_KEY_REDLOCK);
        }



        return "multiLock is over：" + taskThreadID;
    }


}
