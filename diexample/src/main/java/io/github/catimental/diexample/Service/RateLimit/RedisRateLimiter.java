package io.github.catimental.diexample.Service.RateLimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    public RedisRateLimiter(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setResultType(Long.class);


        this.script.setScriptText(
            // "local current = redis.call('INCR', KEYS[1])\n" + 
            // "if tonumber(current) == 1 then\n" + 
            // " redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +
            // "end\n" + 
            // "return current\n"

            "local current = redis.call('INCR', KEYS[1])\n" +
            "if current == 1 then\n" +
            "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +
            "end\n" +
            "return current\n"
        );
        }


    public long incrementAndGet(String key, int windowSeconds){
        Long result = redisTemplate.execute(script, List.of(key), String.valueOf(windowSeconds));
        return result == null ? 0L : result;
    }

    public Long getTtSeconds(String key){
        return redisTemplate.getExpire(key);
    }

}
