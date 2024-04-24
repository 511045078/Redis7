package com.atguigu.redislock.service;

import com.atguigu.redislock.mylock.DistributedLockFactory;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

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
    @Autowired
    private DistributedLockFactory distributedLockFactory;


    /**
     * TODO V9.0,引入Redisson官网推荐的RedLock算法实现类
     *
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/22 14:25
     */

    @Autowired
    private Redisson redisson;
    public String saleByRedisson()
    {
        String retMessage = "";
        Lock redisLock = distributedLockFactory.getDistributedLock("redis");

        RLock redissonLock = redisson.getLock("lxfRedisLock");
        redissonLock.lock();

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
            if (redissonLock.isLocked() && redissonLock.isHeldByCurrentThread()){
                redissonLock.unlock();
            }
        }
        return retMessage+"\t"+"服务端口号："+port;
    }


    /**
     * TODO V8.0,实现自动续期功能的完善，后台自定义扫描程序，如果规定时间内没完成业务逻辑，会调用加钟自动续期的脚本
     *
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/22 10:18
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     Lock redisLock = distributedLockFactory.getDistributedLock("redis");
    //     redisLock.lock();
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
    //             // 暂停120秒,故意的，暂停120秒钟
    //             try { TimeUnit.SECONDS.sleep(120); } catch (InterruptedException e) { e.printStackTrace(); }
    //         }else{
    //             retMessage = "商品卖完了，o(╥﹏╥)o";
    //         }
    //     }finally {
    //         redisLock.unlock();
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }

    /**
     * TODO V7.0
     *
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/19 15:02
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     Lock redisLock = distributedLockFactory.getDistributedLock("redis");
    //     redisLock.lock();
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
    //             testReEntry();
    //         }else{
    //             retMessage = "商品卖完了，o(╥﹏╥)o";
    //         }
    //     }finally {
    //         redisLock.unlock();
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }

    private void testReEntry()
    {
        Lock lock = distributedLockFactory.getDistributedLock("redis");
        lock.lock();
        try {
            System.out.println("============测试可重入锁==============");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

    }

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
     * TODO V3.2 存在的问题是：
     * 获取锁的时候，没有设置过期时间，如果获取锁后，微服务宕机，则其他微服务无法再次获得锁，所以需要把锁加一个过期时间
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:11
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     String key = "lxfRedisLock";
    //     String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
    //     // 不用递归了，高并发下容易发生stackoverflowerror，我们用自旋代替递归方法重试调用，也不用if了，用while替代
    //     while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue))
    //     {
    //         // 暂停毫秒
    //         try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
    //     }
    //     // 获取锁成功
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
    //         stringRedisTemplate.delete(key);
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }
    
    /**
     * TODO V4.0 出现的问题：张冠李戴，线程1创建的锁，30秒过期后，线程1还没干完活，这个时候锁过期，线程2创建锁，32秒时，线程1干完活，不管不顾把锁一删，其实删除的是线程2的锁，程序乱了
     * stringRedisTemplate.delete(key);这里只让自己删自己的锁
     * 防止微服务宕机造成的死锁
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:24
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     String key = "lxfRedisLock";
    //     String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
    //     // 不用递归了，高并发下容易发生stackoverflowerror，我们用自旋代替递归方法重试调用，也不用if了，用while替代
    //     while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS))
    //     {
    //         // 暂停毫秒
    //         try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
    //     }
    //     // 加锁和过期时间必须在同一行，保证原子性
    //     // 锁建立成功立刻加入过期时间,这样会导致代码间存在缝隙，并没有保持原子性，高并发下依旧会出现死锁问题,
    //     // stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);
    //     // 获取锁成功
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
    //         stringRedisTemplate.delete(key);
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }

    /**
     * TODO V5.0版本，改进点只能删除属于自己的Key，不能删除别人的
     *
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:40
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     String key = "lxfRedisLock";
    //     String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
    //     // 不用递归了，高并发下容易发生stackoverflowerror，我们用自旋代替递归方法重试调用，也不用if了，用while替代
    //     while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS))
    //     {
    //         // 暂停毫秒
    //         try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
    //     }
    //     // 加锁和过期时间必须在同一行，保证原子性
    //     // 锁建立成功立刻加入过期时间,这样会导致代码间存在缝隙，并没有保持原子性，高并发下依旧会出现死锁问题,
    //     // stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);
    //     // 获取锁成功
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
    //         if (stringRedisTemplate.opsForValue().get(key).equalsIgnoreCase(uuidValue)){
    //             // 非原子操作，隐藏风险
    //             stringRedisTemplate.delete(key);
    //         }
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }

    /**
     * TODO V6.0，if+delete非原子性，lua脚本把redis变成原子操作
     * 不满足可重入性
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/18 15:49
     */
    // public String sale()
    // {
    //     String retMessage = "";
    //     String key = "lxfRedisLock";
    //     String uuidValue = IdUtil.simpleUUID()+":"+ Thread.currentThread().getId();
    //     // 不用递归了，高并发下容易发生stackoverflowerror，我们用自旋代替递归方法重试调用，也不用if了，用while替代
    //     while (!stringRedisTemplate.opsForValue().setIfAbsent(key, uuidValue, 30L, TimeUnit.SECONDS))
    //     {
    //         // 暂停毫秒
    //         try { TimeUnit.MILLISECONDS.sleep(20); }catch (InterruptedException e){ e.printStackTrace(); }
    //     }
    //     // 加锁和过期时间必须在同一行，保证原子性
    //     // 锁建立成功立刻加入过期时间,这样会导致代码间存在缝隙，并没有保持原子性，高并发下依旧会出现死锁问题,
    //     // stringRedisTemplate.expire(key,30L,TimeUnit.SECONDS);
    //     // 获取锁成功
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
    //         // 改进点，改用lua脚本编写判断和删除，以适配redis原子性
    //
    //         String luaScript =
    //                 "if redis.call('get',KEYS[1]) == ARGV[1] then " +
    //                     "return redis.call('del', KEYS[1]) " +
    //                 "else " +
    //                     "return 0 " +
    //                 "end";
    //         stringRedisTemplate.execute(new DefaultRedisScript<>(luaScript,Boolean.class), Arrays.asList(key),uuidValue);
    //         // if (stringRedisTemplate.opsForValue().get(key).equalsIgnoreCase(uuidValue)){
    //         //     // 非原子操作，隐藏风险
    //         //     stringRedisTemplate.delete(key);
    //         // }
    //     }
    //     return retMessage+"\t"+"服务端口号："+port;
    // }

}
