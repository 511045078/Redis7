package com.atguigu.redis7.controller;

import com.atguigu.redis7.service.HyperLogLogService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/*
 * 统计数量，去重，一般需求为访客量
 * 用hyperloglog是因为数量巨大，他只存一个不是非常准确的数字，误差在0.8%左右
 */

@Api(tags = "淘宝亿级UV的Redis统计方案")
@RestController
@Slf4j
public class HyperLogLogController {

    @Resource
    private HyperLogLogService hyperLogLogService;

    @GetMapping(value = "/uv")
    public long uv(){
        return hyperLogLogService.uv();
    }

}
