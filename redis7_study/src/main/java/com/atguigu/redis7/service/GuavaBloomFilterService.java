package com.atguigu.redis7.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Random;

@Service
@Slf4j
public class GuavaBloomFilterService {
    public static final int _1W = 10000;
    //布隆过滤器里预计要插入多少数据
    public static int SIZE = 100 * _1W;
    //误判率,它越小误判的个数也就越少(思考，是不是可以设置的无限小，没有误判岂不更好)
    //fpp the desired false positive probability
    public static double fpp = 0.03;
    // 构建布隆过滤器
    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), SIZE,fpp);

    /*
    * 白名单
    *  */
    public void guavaBloomFilter(){
        // 1 先给过滤器加入初始值
        for (int i = 0; i < SIZE; i++) {
            bloomFilter.put(i);
        }
        // 2 故意取10W个不在合法范围内的数据，进行误判率演示
        ArrayList<Integer> list = new ArrayList<>(10 * _1W);
        for (int i = SIZE; i < SIZE + (10 * _1W); i++) {
            if (bloomFilter.mightContain(i)){
                log.info("被误判了：{}" + i);
                list.add(i);
            }
        }
        log.info("误判数量：{}"+ list.size());
    }

    /*
    * 黑名单
    *  */
    public void guavaBlommFilterBlack(){
        int size = 0;
        for (int i = 0; i < 9999; i++) {
            // 生成视频id
            int id = new Random().nextInt(9999);
            // 判断这个视频看没看过，看过则不看,没看过就把ID加入黑名单
            if (bloomFilter.mightContain(i)){
                log.info("视频ID为："+ id +" 已经看过ID为"+id+"的视频了，不在推荐");
                size ++;
                continue;
            }else {
                bloomFilter.put(id);
                // log.info("推荐");
            }
        }
        log.info("共拒绝了"+ size +"个视频");
    }

}
