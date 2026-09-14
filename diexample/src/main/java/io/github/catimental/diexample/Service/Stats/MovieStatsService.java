package io.github.catimental.diexample.Service.Stats;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import io.github.catimental.diexample.DTO.Stats.MovieStatsResponse;
import io.github.catimental.diexample.Repository.Like.MovieLikeRepository;


@Service
@RequiredArgsConstructor
public class MovieStatsService {
    private final StringRedisTemplate redisTemplate;
    private final MovieLikeRepository movieLikeRepository;


    public MovieStatsResponse getMovieStats(Long movieId){
        String statsKey = "movie:" + movieId + ":stats";

        Object cachedLikeCount = redisTemplate.opsForHash()
                                    .get(statsKey, "likeCount");


        if(cachedLikeCount != null){
            long likeCount = Long.parseLong(cachedLikeCount.toString());
            return new MovieStatsResponse(movieId, likeCount);
        }


        long dbLikeCount = movieLikeRepository.countByMovieIdAndLikeTrue(movieId);


        redisTemplate.opsForHash().put(statsKey, "likeCount", String.valueOf(dbLikeCount));


        return new MovieStatsResponse(movieId, dbLikeCount);
    }
    
}
