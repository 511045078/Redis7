package com.atguigu.redis7.controller;

import cn.hutool.core.util.IdUtil;
import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Liu
 * @apiNote 发红包，抢红包，记红包
 * @date 2024/4/23 17:27
 */
@RestController
@Slf4j
public class RedPackageController
{

    // 发红包KEY
    public static final String RED_PACKAGE_KEY = "redpackage:";

    // 记录红包KEY
    public static final String RED_PACKAGE_CONSUME_KEY = "redpackage:consume:";

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * TODO 发红包
     *
     * @param totalMoney
     * @param redPackageNumber
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/24 10:02
     */
    @RequestMapping("/send")
    public String sendRedPackage(int totalMoney,int redPackageNumber){
        // 将总金额totalMoney拆分为redpackageNumber个子红包
        Integer[] splitRedpackages = splitRedpackageAlgorithm(totalMoney,redPackageNumber);

        // 2 发红包并保存进list结构里
        String key = RED_PACKAGE_KEY + IdUtil.simpleUUID();
        // 放到list结构里，放置过期时间
        redisTemplate.opsForList().leftPushAll(key,splitRedpackages);
        redisTemplate.expire(key,1, TimeUnit.DAYS);
        // 发红包OK，返回前台显示
        return key + "\t" + Ints.asList(Arrays.stream(splitRedpackages).mapToInt(Integer::valueOf).toArray());
    }

    /**
     * TODO 抢红包
     *
     * @param redPackageKey
     * @param userId
     * @return java.lang.String
     * @author Liu
     * @date 2024/4/24 10:29
     */
    @RequestMapping(value = "/rob")
    public String robRedPackage(String redPackageKey, String userId){
        // 抢红包，先判断抢没抢过
        Object redPackage = redisTemplate.opsForHash().get(RED_PACKAGE_CONSUME_KEY+redPackageKey,userId);
        // 没抢过
        if (redPackage == null)
        {
            // 从list里lpop出一个
            Object partRedPackage = redisTemplate.opsForList().leftPop(RED_PACKAGE_KEY + redPackageKey);
            // 判断还有没有
            if (partRedPackage != null)
            {
                // 有，出了一个
                // 记录到hash里去
                redisTemplate.opsForHash().put(RED_PACKAGE_CONSUME_KEY + redPackageKey ,userId, partRedPackage);
                System.out.println("用户: "+userId+"\t 抢到多少钱红包: "+partRedPackage);
                //TODO 后续异步进mysql或者RabbitMQ进一步处理
                return String.valueOf(partRedPackage);
            }
            return "抢完了";
        }
        // 抢过
        return "已经抢过了，不能在抢了";
    }

    /**
     * TODO 拆红包算法-->二倍均值算法
     *
     * @param totalMoney
     * @param redPackageNumber
     * @return java.lang.Integer[]
     * @author Liu
     * @date 2024/4/24 10:03
     */
    private Integer[] splitRedpackageAlgorithm(int totalMoney,int redPackageNumber){
        Integer[] redPackageNumbers = new Integer[redPackageNumber];
        // 已经被抢过的红包金额
        int useMoney = 0;
        for (int i = 0; i < redPackageNumber; i++)
        {
            // 最后一个红包把剩下的钱赛里，不用算了
            if (i == redPackageNumber-1)
            {
                redPackageNumbers[i] = totalMoney - useMoney;
            }else {
                // 二倍均值算法，每次拆分塞进子红包的金额
                // 金额等于随机数（0，剩余红包金额/为被抢的剩余红包个数N * 2）
                int avgMoney = (totalMoney - useMoney) / (redPackageNumber - i) * 2;
                redPackageNumbers[i] = 1 + new Random().nextInt(avgMoney - 1);
            }
            useMoney = useMoney + redPackageNumbers[i];
        }
        return redPackageNumbers;
    }


}
