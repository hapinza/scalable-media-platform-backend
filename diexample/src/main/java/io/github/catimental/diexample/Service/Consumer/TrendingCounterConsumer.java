package io.github.catimental.diexample.Service.Consumer;

import org.springframework.stereotype.Service;

import io.github.catimental.diexample.DTO.trending.TrendingItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Service
@Slf4j
public class TrendingCounterConsumer {

    private final StringRedisTemplate stringRedisTemplate;


    public TrendingCounterConsumer(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @KafkaListener(topics = "trending-topic", groupId = "trending-counter-group")
    public void listen(TrendingItemRequest event){
        //cacheKey
        String key = "trending:count:movie" + event.movieId();

        // add a key
        stringRedisTemplate.opsForValue().increment(key);

        //set expiration date
        stringRedisTemplate.expire(key, Duration.ofDays(30));

        // log
        log.debug("incremented {}", key);
    }



    
}
