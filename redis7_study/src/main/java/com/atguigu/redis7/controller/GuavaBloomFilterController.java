package com.atguigu.redis7.controller;

import com.atguigu.redis7.service.GuavaBloomFilterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/*
* guava布隆过滤器的使用（主流）
*  */
@Api(tags = "google工具Guava处理布隆过滤器")
@RestController
@Slf4j
public class GuavaBloomFilterController {

    @Resource
    private GuavaBloomFilterService guavaBloomFilterService;

    @ApiOperation("guava布隆过滤器插入100万样本数据并额外10W测试是否存在")
    @RequestMapping(value = "/guavafilter",method = RequestMethod.GET)
    public void guavaBloomFilter(){
        guavaBloomFilterService.guavaBloomFilter();
    }

    @ApiOperation("guava布隆过滤器，刷抖音后，把视频ID存进过滤器，不在推荐，黑名单")
    @RequestMapping(value = "/guavafilterBlack",method = RequestMethod.GET)
    public void guavaBlommFilterBlack(){
        guavaBloomFilterService.guavaBlommFilterBlack();
    }

}
