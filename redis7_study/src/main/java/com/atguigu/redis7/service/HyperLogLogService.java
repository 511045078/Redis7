package com.atguigu.redis7.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HyperLogLogService {

    @Resource
    private RedisTemplate redisTemplate;

    /*
     * 模拟后台由用户点击淘宝首页，每个用户不同IP
     */
    @PostConstruct
    public void initIP(){
        new Thread(() -> {
            String ip = null;//255.255.255.255
            for (int i = 0; i < 200; i++) {
                Random random = new Random();
                ip = random.nextInt(256)+"."+random.nextInt(256)+"."+random.nextInt(256)+"."+random.nextInt(256);
                Long hll = redisTemplate.opsForHyperLogLog().add("hll",ip);
                log.info("ip={},该IP地址访问首页次数={}",ip,hll);
            try{TimeUnit.SECONDS.sleep(3);} catch(InterruptedException e) {e.printStackTrace();}
            }
        }, "t1").start();
    }


    /*
     * 统计UV（用户访问量，去重）
     */
    public long uv(){
        return redisTemplate.opsForHyperLogLog().size("hll");
    }

}
