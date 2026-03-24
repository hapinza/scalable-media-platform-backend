package io.github.catimental.diexample.Consumer.Redis;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.catimental.diexample.Repository.FailedEventRepository;
import io.github.catimental.diexample.Repository.ProcessedEventRepository;
import io.github.catimental.diexample.Service.ViewAggregationService;
import io.github.catimental.diexample.domain.event.ProcessedEvent;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.StreamOffset;
import java.time.Duration;
import io.github.catimental.diexample.domain.event.FailedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class MovieViewedConsumer {
    private final StringRedisTemplate redisTemplate;
    private final ViewAggregationService viewAggregationService;
    private final ProcessedEventRepository processedEventRepository;
    private final FailedEventRepository failedEventRepository;
    private final ObjectMapper objectMapper;

    
    private static final String STREAM = "stream:movie:viewed";
    private static final String GROUP = "movie-analytics-group";
    private static final String CONSUMER = "consumer-1";

    @Scheduled(fixedDelay = 2000)
    public void consume(){
        /*
        String → stream (stream:movie:viewed)
        Object → field (eventId, movieId)
        Object → value ("10", "abc123")
        
        */
        List<MapRecord<String,Object,Object>> messages = 
            redisTemplate.opsForStream().read(
                Consumer.from(GROUP, CONSUMER),
                StreamReadOptions.empty().count(10).block(Duration.ofSeconds(1)),
                StreamOffset.create(STREAM, ReadOffset.lastConsumed())
            );

        if(messages == null || messages.isEmpty()){
            return ;
        }

        for(MapRecord<String, Object, Object> msg : messages){
            String eventId = null;
            try{
                Map<Object, Object> value = msg.getValue();

                eventId = (String)value.get("eventId");
                Long movieId = Long.valueOf((String) value.get("movieId"));
                String memberRaw = (String) value.get("memberId");
                Long memberId = (memberRaw == null || memberRaw.isBlank()) ? null : Long.valueOf(memberRaw);

                if (movieId == 999L) {
                    throw new RuntimeException("forced test exception");
                }


                //processed_event 
                processedEventRepository.save(new ProcessedEvent(eventId));


                viewAggregationService.processedViewEvent(eventId, memberId, movieId);

                redisTemplate.opsForStream().acknowledge(STREAM, GROUP, msg.getId());
                // complete reading
            }catch(DataIntegrityViolationException e){
                log.info("Duplicate event skipped. eventId ={}", eventId);
                redisTemplate.opsForStream().acknowledge(STREAM, GROUP, msg.getId());
            }catch(Exception e){
                log.error("Failed to process stream message id = {} ", msg.getId(), e);

                try{
                    failedEventRepository.save(new FailedEvent(
                        eventId == null ? "unknown" : eventId,
                        safePayload(msg),
                        e.getMessage()
                    ));
                }catch(Exception saveEx){
                    log.error("Failed to persist failed event. eventId ={}", eventId, saveEx);
                }


            }
        }


    }

    private String safePayload(MapRecord<String, Object, Object> msg){
        try{
            return objectMapper.writeValueAsString(msg.getValue());
        }catch(Exception e){
            return String.valueOf(msg.getValue());
        }

    }







    
}
