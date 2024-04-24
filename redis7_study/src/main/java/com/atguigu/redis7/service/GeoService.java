package com.atguigu.redis7.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeoService {

    public static final String CITY ="city";

    @Resource
    private RedisTemplate redisTemplate;

    public String geoAdd() {
        Map<String, Point> map = new HashMap<>();
        map.put("天安门",new Point(116.403963,39.915119));
        map.put("故宫",new Point(116.403414,39.924091));
        map.put("长城",new Point(116.024067,40.362639));
        redisTemplate.opsForGeo().add(CITY,map);
        return map.toString();
    }

    public Point position(String member) {
        // 获取经纬度坐标
        List<Point> position = redisTemplate.opsForGeo().position(CITY, member);
        return position.get(0);
    }

    public String hash(String member) {
        // geohash算法生成的base32编码值
        List<String> hash = redisTemplate.opsForGeo().hash(CITY,member);
        return hash.get(0);
    }

    public Distance distance(String member1, String member2) {
        // 获取两个给定位置之间的距离
        Distance distance = redisTemplate.opsForGeo().distance(CITY, member1, member2, RedisGeoCommands.DistanceUnit.KILOMETERS);
        return distance;
    }

    public GeoResults radiusByxy() {
        // 通过经度纬度查找北京王府井附近的50条
        Circle circle = new Circle(116.417622, 39.920672, Metrics.KILOMETERS.getMultiplier());
        //返回50条
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().sortDescending().limit(50);
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo().radius(CITY, circle, args);
        return geoResults;
    }

    public GeoResults radiusByMember() {
        // 通过地方查找附近,本例写死天安门作为地址
        Circle circle = new Circle(position("天安门"), Metrics.KILOMETERS.getMultiplier());
        // Circle circle = new Circle(116.417622, 39.920672,Metrics.KILOMETERS.getMultiplier());
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().limit(50);
        GeoResults<RedisGeoCommands.GeoLocation<String>> radius = redisTemplate.opsForGeo().radius(CITY, circle, args);
        return radius;
    }


}
