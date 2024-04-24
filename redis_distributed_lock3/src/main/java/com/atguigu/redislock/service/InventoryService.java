package com.atguigu.redislock.service;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @auther zzyy
 * @create 2022-10-22 15:14
 */
@Service
@Slf4j
public class InventoryService
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String port;

    private Lock lock = new ReentrantLock();

    /**
     * TODO V2.0版本，单机版加锁，配合Nginx和Jmeter不符合压力测试，加锁失败
     *
     * @param null
     * @return null
     * @date 2024/4/18 15:04
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     lock.lock();
    //     try
    //     {
    //         //1 查询库存信息
    //         String result = stringRedisTemplate.opsForValue().get("inventory001");
    //         //2 判断库存是否足够
    //         Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
    //         //3 扣减库存
    //         if(inventoryNumber > 0) {
    //             stringRedisTemplate.opsForValue().set("inventory001",String.valueOf(--inventoryNumber));
    //             retMessage = "成功卖出一个商品，库存剩余: "+inventoryNumber;
    //             System.out.println(retMessage);
    //         }else{
    //             retMessage = "商品卖完了，o(╥﹏╥)o";
    //         }
    //     }finally {
    //         lock.unlock();
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }


    /**
     * TODO V3.1版本，完成递归重试功能，容易导致stackoverflowerror，所以不太推荐；另外，高并发唤醒后推荐用while而非if
     * 功能实现，但是性能不行，需要优化递归调用和if
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:08
     */
    // public String sale(){
    //     String retMessage = "";
    //     String key = "lxfRedisLock";
    //     String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
    //     Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue);
    //     if (!flag)
    //     {
    //         //获取锁失败，暂停20毫秒后，重试
    //         // 暂停毫秒
    //         try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
    //         sale();
    //     }else
    //     {
    //         // 获取锁成功
    //         try
    //         {
    //             //1 查询库存信息
    //             String result = stringRedisTemplate.opsForValue().get("inventory001");
    //             //2 判断库存是否足够
    //             Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
    //             //3 扣减库存
    //             if(inventoryNumber > 0) {
    //                 stringRedisTemplate.opsForValue().set("inventory001",String.valueOf(--inventoryNumber));
    //                 retMessage = "成功卖出一个商品，库存剩余: "+inventoryNumber;
    //                 System.out.println(retMessage);
    //             }else{
    //                 retMessage = "商品卖完了，o(╥﹏╥)o";
    //             }
    //         }finally {
    //             stringRedisTemplate.delete(key);
    //         }
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }


    /**
     * TODO V3.2
     *
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:11
     */
    public String sale()
    {
        String retMessage = "";
        String key = "lxfRedisLock";
        String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
        // 不用递归了，高并发下容易发生stackoverflowerror，我们用自旋代替递归方法重试调用，也不用if了，用while替代
        while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue))
        {
            // 暂停毫秒
            try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
        }
        // 获取锁成功
        try
        {
            //1 查询库存信息
            String result = stringRedisTemplate.opsForValue().get("inventory001");
            //2 判断库存是否足够
            Integer inventoryNumber = result == null ? 0 : Integer.parseInt(result);
            //3 扣减库存
            if(inventoryNumber > 0) {
                stringRedisTemplate.opsForValue().set("inventory001",String.valueOf(--inventoryNumber));
                retMessage = "成功卖出一个商品，库存剩余: "+inventoryNumber;
                System.out.println(retMessage);
            }else{
                retMessage = "商品卖完了，o(╥﹏╥)o";
            }
        }finally {
            stringRedisTemplate.delete(key);
        }
        return retMessage+"\t"+"服务端口号："+port;
    }

}
