package com.atguigu.redis7.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.atguigu.redis7.entities.Product;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JHSTaskService {

    public  static final String JHS_KEY="jhs";
    public  static final String JHS_KEY_A="jhs:a";
    public  static final String JHS_KEY_B="jhs:b";

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 偷个懒不加mybatis了，模拟从数据库读取100件特价商品，用于加载到聚划算的页面中
     * @return
     */
    private List<Product> getProductsFromMysql() {
        List<Product> list=new ArrayList<>();
        for (int i = 1; i <=20; i++) {
            Random rand = new Random();
            int id= rand.nextInt(10000);
            Product obj=new Product((long) id,"product"+i,i,"detail");
            list.add(obj);
        }
        return list;
    }

    // @PostConstruct
    // public void initJHS(){
    //     log.info("启动定时器，聚划算功能开始");
    //     // 1 用线程模拟定时任务，后台任务，定时将mysql里面参加活动的商品，添加到redis里
    //     new Thread(()-> {
    //         while (true){
    //             // 2 模拟从mysql查出数据，用于加载到redis并给聚划算到页面
    //             List<Product> list = this.getProductsFromMysql();
    //             // 3 利用redis lish的lpush实现存储
    //             redisTemplate.delete(JHS_KEY);
    //             // 4 加入最新数据给redis用
    //             redisTemplate.opsForList().leftPushAll(JHS_KEY,list);
    //             // 5 暂停1分钟线程，模拟聚划算一天参加活动的品牌
    //             try {
    //                 TimeUnit.MINUTES.sleep(1);
    //             }catch (InterruptedException e){
    //                 e.printStackTrace();
    //             }
    //         }
    //     }, "t1").start();
    // }

    /*
    * 防止缓存在删除key的时候，大量请求进来打到mysql，防止缓存击穿
    * 所以采用双缓存机制
    *  */
    @PostConstruct
    public void initJHSAB(){
        log.info("启动双缓存定时器，聚划算功能开始");
        // 1 用线程模拟定时任务，后台任务，定时将mysql里面参加活动的商品，添加到redis里
        new Thread(()-> {
            while (true){
                // 2 模拟从mysql查出数据，用于加载到redis并给聚划算到页面
                List<Product> list = this.getProductsFromMysql();
                // 3 双缓存机制，先更新B，且让B的过期时间超过A，如果A突然失效，还有B兜底，防止缓存击穿
                redisTemplate.delete(JHS_KEY_B);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_B,list);
                redisTemplate.expire(JHS_KEY_B, 86410L, TimeUnit.SECONDS);
                // 4 双缓存机制，在更新A，
                redisTemplate.delete(JHS_KEY_A);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_A,list);
                redisTemplate.expire(JHS_KEY_A, 86400L, TimeUnit.SECONDS);
                // 5 暂停1分钟线程，模拟聚划算一天参加活动的品牌
                try {
                    TimeUnit.MINUTES.sleep(1);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, "t1").start();
    }

}
