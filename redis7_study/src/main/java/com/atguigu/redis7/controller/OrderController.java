package com.atguigu.redis7.controller;

import com.atguigu.redis7.service.OrderService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "订单接口")
@RestController
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping(value = "/order/add")
    public void addOrder(){
        orderService.addOrder();
    }

    @GetMapping(value = "/order/{keyId}")
    public String getOrderById(@PathVariable Integer keyId){
        return orderService.getOrderById(keyId);
    }


}
