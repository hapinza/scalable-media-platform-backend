package io.github.catimental.diexample.Consumer.Redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.time.Duration;



@Slf4j
@Component
@RequiredArgsConstructor
public class MovieLikeAggregationConsumer {
    private final StringRedisTemplate redisTemplate;

    private static final String STREAM = "stream:movie:liked";
    private static final String GROUP = "movie-like-aggregation-group";
    private static final String CONSUMER = "movie-like-aggregation-consumer";


    @Scheduled(fixedDelay = 1000)
    public void consume(){
        try{
            List<MapRecord<String, Object, Object>> records = 
                redisTemplate.opsForStream().read(
                    // Read up to 10 unprocessed events from the specified Redis Stream
                    // as a member of the given Consumer Group and Consumer.
                    org.springframework.data.redis.connection.stream.Consumer.from(GROUP, CONSUMER),
                    org.springframework.data.redis.connection.stream.StreamReadOptions.empty().count(10),
                    org.springframework.data.redis.connection.stream.StreamOffset.create(STREAM, org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed())
                );

                if(records == null || records.isEmpty()){
                    return ;
                }

                for(MapRecord<String, Object, Object> record: records){
                    handle(record);
                }       
        }catch(Exception e){
            log.warn("Failed to consume movie like aggregation events", e);
        }
    }

    /*
    MapRecord<
    String,  // Stream Key
    Object,  // Map Key
    Object   // Map Value
    */
    private void handle(MapRecord<String, Object, Object> record){
            Map<Object, Object> value = record.getValue();

            String eventId = String.valueOf(value.get("eventId"));
            String movieId = String.valueOf(value.get("movieId"));
            long delta = Long.parseLong(String.valueOf(value.get("delta")));

            String passedKey = "processed:event" + eventId;

            // if it hasn't been processed, create 1
            // if is, acknoledge
            // idempotency 
            Boolean firstTime = redisTemplate.opsForValue().
                                setIfAbsent(passedKey, "1", Duration.ofDays(7));


            if(!Boolean.TRUE.equals(firstTime)){
                redisTemplate.opsForStream().acknowledge(STREAM, GROUP, record.getId());
                return ;
            }


            String statsKey = "movie:" + movieId + ":stats";

            redisTemplate.opsForHash()
                            .increment(statsKey, "likeCount", delta);

            redisTemplate.opsForStream()
                            .acknowledge(STREAM, GROUP, record.getId());


           log.info("Aggregated movie like event. movieId = {} , delta = {} ", movieId, delta);
    }
}


