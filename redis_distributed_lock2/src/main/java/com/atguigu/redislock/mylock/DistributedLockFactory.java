package com.atguigu.redislock.mylock;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

/**
 * @author Liu
 * @apiNote
 * @date 2024/4/19 16:11
 */

@Component
public class DistributedLockFactory
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private String lockName;
    private String uuid;

    public DistributedLockFactory()
    {
        this.uuid = IdUtil.simpleUUID();
    }

    public Lock getDistributedLock(String lockType)
    {
        if (lockType == null) return null;
        if (lockType.equalsIgnoreCase("REDIS"))
        {
            this.lockName = "lxfRedisLock";
            return new RedisDistributedLock(stringRedisTemplate,lockName,uuid);
        }else if(lockType.equalsIgnoreCase("ZOOKEEPER"))
        {
            this.lockName = "lxfZookeeperLock";
            // TODO zookeeper版本的分布式锁
            return null;
        }else if(lockType.equalsIgnoreCase("MYSQL"))
        {
            this.lockName = "lxfMysqlLock";
            // TODO MYSQL版本的分布式锁
            return null;
        }
        return null;
    }
}
