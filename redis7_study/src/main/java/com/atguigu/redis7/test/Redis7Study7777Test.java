package com.atguigu.redis7.test;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableSwagger2
@MapperScan("com.atguigu.redis7.mapper") //import tk.mybatis.spring.annotation.MapperScan;
public class Redis7Study7777Test {
    public static void main(String[] args) {
        SpringApplication.run(Redis7Study7777Test.class, args);
    }



    @Test
    public void testGuavaWithBloomFilter(){
        // 1 创建guava版布隆过滤器
        BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 100);
        // 2 判断指定的元素是否存在
        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));
        bloomFilter.put(1);
        bloomFilter.put(2);
        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));



    }

}