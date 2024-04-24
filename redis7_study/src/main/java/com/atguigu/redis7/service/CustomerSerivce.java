package com.atguigu.redis7.service;

import com.atguigu.redis7.entities.Customer;
import com.atguigu.redis7.mapper.CustomerMapper;
import com.atguigu.redis7.utils.CheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class CustomerSerivce {

    public static final String CACHE_KEY_CUSTOMER = "customer:";

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CustomerMapper customerMapper;

    @Resource
    private CheckUtils checkUtils;

    /*
    * 写操作
     */
    public void addCustomer(Customer customer){
        int i = customerMapper.insertSelective(customer);
        if (i>0){
            // mysql插入成功，需要重新查询一次
            Customer result = customerMapper.selectByPrimaryKey(customer.getId());
            // redis缓存key
            String key = CACHE_KEY_CUSTOMER + result.getId();
            // 捞出来的数据，写进redis
            redisTemplate.opsForValue().set(key,result);
        }
    }

    /*
    * 读操作
     */
    public Customer findCustomerById(Integer customerId){
        Customer customer = null;
        String key = CACHE_KEY_CUSTOMER + customerId;
        // 1 先查redis
        customer = (Customer) redisTemplate.opsForValue().get(key);
        // 2 redis有直接返回，没有查询mysql
        if (customer == null ){
            // 3 查mysql
            customer = customerMapper.selectByPrimaryKey(customerId);
            // 3.1 mysql有，redis无，
            if (customer!=null){
                // 3.2 把mysql查询出来的数据回写redis，双写一致性
                redisTemplate.opsForValue().set(key,customer);
            }
        }
        return customer;
    }


    /**
     * BloomFilter → redis → mysql
     * 白名单：whitelistCustomer
     * @param customerId
     * @return
     */

    public Customer findCustomerByIdWithBloomFilter (Integer customerId){
        Customer customer = null;
        String key = CACHE_KEY_CUSTOMER + customerId;

        //布隆过滤器，有是可能有，无是必定无
        // ============================
        if (!checkUtils.checkWithBloomFilter("whitelistCustomer", key)){
            log.info("白名单无此顾客，不可以访问" + key);
            return null;
        }
        // ============================

        // 1 先查redis
        customer = (Customer) redisTemplate.opsForValue().get(key);
        // 2 redis有直接返回，没有查询mysql
        if (customer == null ){
            // 3 查mysql
            customer = customerMapper.selectByPrimaryKey(customerId);
            // 3.1 mysql有，redis无，
            if (customer!=null){
                // 3.2 把mysql查询出来的数据回写redis，双写一致性
                redisTemplate.opsForValue().set(key,customer);
            }
        }
        return customer;
    }


}
