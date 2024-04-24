package com.atguigu.redis7.controller;

import com.atguigu.redis7.entities.Product;
import com.atguigu.redis7.service.JHSTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "聚划算商品列表接口")
@RestController
@Slf4j
public class JHSProductController {
    public  static final String JHS_KEY="jhs";
    public  static final String JHS_KEY_A="jhs:a";
    public  static final String JHS_KEY_B="jhs:b";

    @Resource
    private JHSTaskService jhsTaskService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 分页查询：在高并发的情况下，只能走redis查询，走db的话必定会把db打垮
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/pruduct/find",method = RequestMethod.GET)
    @ApiOperation("按照分页和每页显示容量，点击查看，每次")
    public List<Product> find(int page, int size) {
        List<Product> list = null;
        long start = (page-1) * size;
        long end = start + size - 1;
        try {
            //采用redis list结构的lrange实现加载和分页查询
            list = redisTemplate.opsForList().range(JHS_KEY,start,end);
            //如果redis没有
            if(CollectionUtils.isEmpty(list)){
                // 双捡加锁
                // 查mysql
            }
            log.info("参加活动的商家：{}"+ list);
        }catch (Exception e){
            // 出异常，一般redis宕机，或者redis网络抖动，导致timeout
            log.error("jhs exception:{}"+ e);
            e.printStackTrace();
            // 再次查询mysql
        }
        return null;
    }

    /**
     * 分页查询：在高并发的情况下，只能走redis查询，走db的话必定会把db打垮
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/pruduct/findAB",method = RequestMethod.GET)
    @ApiOperation("双缓存机制，防止缓存击穿，按照分页和每页显示容量，点击查看，每次")
    public List<Product> findAB(int page, int size) {
        List<Product> list = null;
        long start = (page-1) * size;
        long end = start + size - 1;
        try {
            //采用redis list结构的lrange实现加载和分页查询
            list = redisTemplate.opsForList().range(JHS_KEY_A,start,end);
            //如果redisA没有
            if(CollectionUtils.isEmpty(list)){
                log.info("----A缓存过期，或活动失效，记得人工修改，B缓存继续顶着");
                list = redisTemplate.opsForList().range(JHS_KEY_B,start,end);
                if (CollectionUtils.isEmpty(list)){
                    log.info("---B也过期了，完蛋了");
                    //如果redisB也没有
                    // 双捡加锁
                    // TODO 查mysql
                }
            }
            log.info("参加活动的商家：{}"+ list);
        }catch (Exception e){
            // 出异常，一般redis宕机，或者redis网络抖动，导致timeout
            log.error("jhs exception:{}"+ e);
            e.printStackTrace();
            // 再次查询mysql
        }
        return null;
    }

}
