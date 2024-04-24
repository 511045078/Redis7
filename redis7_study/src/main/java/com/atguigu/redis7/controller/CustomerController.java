package com.atguigu.redis7.controller;

import com.atguigu.redis7.entities.Customer;
import com.atguigu.redis7.service.CustomerSerivce;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

/*
* 手搓布隆过滤器
*  */
@Api(tags = "客户Customer接口+布隆过滤器讲解")
@RestController
@Slf4j
public class CustomerController {

    @Resource
    private CustomerSerivce customerSerivce;

    @ApiOperation("数据库初始化2条customer记录插入")
    @RequestMapping(value = "/customer/add", method = RequestMethod.POST)
    public void addCustomer(){
        for (int i = 0; i < 2; i++) {
            Customer customer = new Customer();
            customer.setCname("customer"+i);
            customer.setAge(new Random().nextInt(30)+1);
            customer.setPhone("1381111xxxx");
            customer.setSex((byte) new Random().nextInt(2));
            customer.setBirth(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            customerSerivce.addCustomer(customer);
        }
    }

    @ApiOperation("单个customer查询操作，按照customerId查询")
    @RequestMapping(value = "/customer/{customerId}", method = RequestMethod.GET)
    public Customer findCustomerById(@PathVariable Integer customerId){
        return customerSerivce.findCustomerById(customerId);
    }


    @ApiOperation("布隆过滤器，白名单过滤")
    @RequestMapping(value = "/customerBloomFilter/{customerId}", method = RequestMethod.GET)
    public Customer findCustomerByIdWithBloomFilter (@PathVariable Integer customerId){
        return customerSerivce.findCustomerByIdWithBloomFilter(customerId);
    }

}
