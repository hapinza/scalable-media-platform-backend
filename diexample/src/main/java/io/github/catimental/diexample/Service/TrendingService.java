package io.github.catimental.diexample.Service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.catimental.diexample.DTO.trending.TrendingPageCache;
import io.github.catimental.diexample.DTO.trending.TrendingItemResponse;
import io.github.catimental.diexample.DTO.trending.TrendingScoreItemResponse;
import io.github.catimental.diexample.Repository.ViewEventRepository;
import io.github.catimental.diexample.exception.*;
import java.time.Duration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.Exception;



import lombok.extern.slf4j.Slf4j;


import org.springframework.data.domain.Pageable;


@Slf4j
@Service
@Transactional(readOnly = true)
public class TrendingService {
    private final ViewEventRepository viewEventRepository;

    private static final long LIKE_WEIGHT = 3L;
    private static final Duration TRENDING_TTL = Duration.ofMinutes(5);

    @Autowired  // cache, data
    private StringRedisTemplate redisTemplate;

    @Autowired  // store in cache with json(string)
    private ObjectMapper objectMapper;



    public void cacheTrendingData(String key, String json, Duration ttl){
        redisTemplate.opsForValue().set(key, json, ttl);
    }

    public String getCachedTrendingData(String movieId){
        return redisTemplate.opsForValue().get(movieId);
    }


    public TrendingService(ViewEventRepository viewEventRepository, StringRedisTemplate redisTemplate, 
        ObjectMapper objectMapper
    ){
        this.viewEventRepository = viewEventRepository;
    }


    public Page<TrendingItemResponse> trending(int windowDays, int page, int size){
        if(windowDays <= 0 || windowDays >30){
            throw new ApiException(ErrorCode.INVALID_WINDOW, "windowDays must be between 1 to 30");
        }

        if(size <= 0 || size >50) size = 20;
        if(page < 0) page = 0;

        String cacheKey = "trending:%d:p%d:s%d".formatted(windowDays, page, size);

        String cachedJson = redisTemplate.opsForValue().get(cacheKey);


        if(cachedJson != null){
            try{
                TrendingPageCache<TrendingItemResponse> cache = objectMapper.readValue(cachedJson, new TypeReference<TrendingPageCache<TrendingItemResponse>>() {
                    
                } );

                Pageable pageable = PageRequest.of(cache.number(), cache.size());

                return new PageImpl<>(cache.items(), pageable, cache.totalElements());
            }catch(Exception e){
                log.warn("Failed to reach cache for Key= {}", cacheKey, e);
            }

        
        }


        LocalDateTime from = LocalDateTime.now().minusDays(windowDays);

        Page<TrendingItemResponse> result = viewEventRepository.findTrending(from, PageRequest.of(page, size))
                                                .map(p -> new TrendingItemResponse(p.getMovieId(), p.getViewCount()));
         // has the same return value map like the DTO(TrendingItemResponse) but
        // we change the generic because of the responsibility and function itself.



        TrendingPageCache<TrendingItemResponse> payload = new TrendingPageCache<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.hasNext()
        );


        try{
            String json = objectMapper.writeValueAsString(payload);
            cacheTrendingData(cacheKey, json, TRENDING_TTL);
        }catch(Exception e){
            log.warn("Failed to store data in cache", payload,e);

        }


        return result; // or payload
        
        // what about change the DTO to trendingpagecache
        // Page, pageof
    }


    
    public Page<TrendingScoreItemResponse> trendingWithScore(int windowDays, int page, int size){
        if(windowDays<= 0 || windowDays > 30){
            throw new ApiException(ErrorCode.INVALID_WINDOW, "windowdays must be between 1 to 30");

        }


        if(size < 0 || size > 50) size = 20;
        if(page < 0) page = 0;

        String cacheKey = "trending:w%d:p%d:s%d".formatted(windowDays, page, size);

        String cached = redisTemplate.opsForValue().get(cacheKey);

        if(cached != null){
            try{
                TrendingPageCache<TrendingScoreItemResponse> cache = objectMapper.readValue(cached, new TypeReference<
                    TrendingPageCache<TrendingScoreItemResponse>>(){});
                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(cache.items(), pageable, cache.totalElements());


                }catch(Exception e){
                    log.warn("Failed to parse cache. key={}", cacheKey, e);
                }
        }


        LocalDateTime from = LocalDateTime.now().minusDays(windowDays);

        Page<TrendingScoreItemResponse> result = viewEventRepository.findTrendingWithScore(
            from, LIKE_WEIGHT, PageRequest.of(page, size)
        ).map(p -> new TrendingScoreItemResponse(
            p.getMovieId(),
            p.getViewCount(),
            p.getLikeCount(),
            p.getScore()));


        try{
            TrendingPageCache<TrendingScoreItemResponse> cache = new TrendingPageCache<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.hasNext()
            );

        String json = objectMapper.writeValueAsString(cache);
        cacheTrendingData(cacheKey, json, TRENDING_TTL);

        }catch(Exception e){
            log.warn("Failed to store data into cahc", e);
        }


        return result;

    }







}







