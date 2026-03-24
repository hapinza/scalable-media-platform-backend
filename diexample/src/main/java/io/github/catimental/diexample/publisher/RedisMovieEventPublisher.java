package io.github.catimental.diexample.publisher;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.catimental.diexample.DTO.trending.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisMovieEventPublisher implements MovieEventPublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public static final String VIEW_EVENT_STREAM = "stream:movie:viewed";


    @Override
    public void publishMovieViewed(MovieViewedEvent event){
        String eventId = UUID.randomUUID().toString();
        try{
            Map<String, String> payload = new HashMap<>();
            payload.put("eventId", eventId);
            payload.put("movieId", String.valueOf(event.movieId()));
            payload.put("memberId", event.memberId() == null ? "" :  String.valueOf(event.memberId()));
            payload.put("occuredAt", String.valueOf(event.occuredAt()));

            redisTemplate.opsForStream().add(VIEW_EVENT_STREAM, payload);

        }catch(Exception e){
            throw new RuntimeException("Failed to publish movie view event", e);
        }


    }



}
