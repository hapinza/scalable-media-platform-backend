package io.github.catimental.diexample.config;

import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamInitializer {
    private final StringRedisTemplate redisTemplate;


    @PostConstruct
    public void init(){
        String streamKey = "stream:movie:viewed";
        String groupName = "movie-analytics-group"; //Consumer Group 1
        // under one tream that conists of several things
        // it has various groups such as (how the information can be processed by group)
        // movie-analytics-group, movie-recommendation-group, movie-logging-group


        try{
            if(Boolean.FALSE.equals(redisTemplate.hasKey(streamKey))){
                redisTemplate.opsForStream().add(streamKey, Map.of("init", "true"));
                // create stream if not. not automatically created
            }
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(),groupName);


        }catch(Exception ignored){}
           



    }



    }
