package com.atguigu.redis7.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

import javax.annotation.Resource;
/**
 * @auther
 * @create
 * 布隆过滤器白名单初始化工具类，一开始就设置一部分数据为白名单所有，
 * 白名单业务默认规定：布隆过滤器有，redis也有。
 */

@Component
@Slf4j
public class BloomFilterInit {

    @Resource
    private RedisTemplate redisTemplate;

    @PostConstruct
    private void init(){
        // 1 白名单，初始化塞到布隆过滤器里
        String key = "customer:10";
        // 2 计算hashValue，由于结果存在负数，所以取绝对值
        int hashValue = Math.abs(key.hashCode());
        // 3 通过hashValue和2的32次方后取余，获得对应的下标坑位
        long index = (long) (hashValue % Math.pow(2,32));
        log.info(key+ "对应的坑位index：{}",index);
        // 4 设置redis里面bitmap的坑位，白名单whitelistCustomer的值设置为1
        redisTemplate.opsForValue().setBit("whitelistCustomer", index, true);

    }

}
