package io.github.catimental.diexample.Service.RateLimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import io.github.catimental.diexample.exception.ApiException;
import io.github.catimental.diexample.exception.ErrorCode;

import java.time.Duration;

@Service
public class RateLimitService {

    private final StringRedisTemplate redis;


    public RateLimitService(StringRedisTemplate redis){
        this.redis = redis;
    }


    // in the timeline(window, usually 1 minute)
    // utilize the cache(redis) for incrementing key in duration(window)
    // key is http request key
    public void checkOrThrow(String key, int limit, Duration window){
        Long count = redis.opsForValue().increment(key);

        if(count != null && count == 1){
            redis.expire(key, window);
        }

        if(count != null && count > 10){
            throw new ApiException(ErrorCode.RATE_LIMITED, "Too many request");
        }
    }




    
}
