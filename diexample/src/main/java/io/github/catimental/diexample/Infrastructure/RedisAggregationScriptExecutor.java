package io.github.catimental.diexample.Infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import java.util.List;


@Component
@RequiredArgsConstructor
public class RedisAggregationScriptExecutor {
    
    private final StringRedisTemplate redisTemplate;


    private static final DefaultRedisScript<Long> IDEMPOTENT_INCREMENT_SCRIPT;



    static{
        IDEMPOTENT_INCREMENT_SCRIPT = new DefaultRedisScript<>();
        IDEMPOTENT_INCREMENT_SCRIPT.setResultType(Long.class);
        IDEMPOTENT_INCREMENT_SCRIPT.setScriptText("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return 0
            end
            
            
            redis.call('HINCRBY', KEYS[2], ARGV[1], ARGV[2])
            redis.call('SET', KEYS[1], '1', 'EX', ARGV[3])

            return 1
                """);
    }


    public long idempotentIncrement(
        String processedKey,
        String statsKey,
        String hashField,
        long delta,
        long processedTtSeconds
    ){
        Long result = redisTemplate.execute(
            IDEMPOTENT_INCREMENT_SCRIPT,
            List.of(processedKey, statsKey),
            hashField,
            String.valueOf(delta),
            String.valueOf(processedTtSeconds)
        );
        return result == null ? 0L : result;
    }




}
