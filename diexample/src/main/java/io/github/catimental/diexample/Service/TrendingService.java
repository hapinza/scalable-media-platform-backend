package io.github.catimental.diexample.Service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.DTO.trending.TrendingItemResponse;
import io.github.catimental.diexample.DTO.trending.TrendingScoreItemResponse;
import io.github.catimental.diexample.Repository.ViewEventRepository;
import io.github.catimental.diexample.exception.ErrorCode;
import io.github.catimental.diexample.exception.*;


@Service
@Transactional(readOnly = true)
public class TrendingService {
    private final ViewEventRepository viewEventRepository;

    private static final long LIKE_WEIGHT = 3L;

    public TrendingService(ViewEventRepository viewEventRepository){
        this.viewEventRepository = viewEventRepository;
    }


    public Page<TrendingItemResponse> trending(int windowDays, int page int size){
        if(windowDays <= 0 || windowDays > 30) {
            throw new ApiException(ErrorCode.INVALID_WINDOW, "windowdays should be between 0 to 30");
        }

        //mistake
        if(size <= 0 || size > 50) size = 20;
        if(page < 0 ) page = 0;


        LocalDateTime from = LocalDateTime.now().minusDays(windowDays);
        return viewEventRepository.findTrending(from, PageRequest.of(page, size))
                            .map(p -> new TrendingItemResponse(p.getMovieId(), p.getViewCount()));
        // has the same return value map like the DTO(TrendingItemResponse) but
        // we change the generic because of the responsibility and function itself.
    }


    public Page<TrendingScoreItemResponse> trendingWithScore(int windowDays, int page, int size){
        if(windowDays <= 0 || windowDays > 30){
            throw new ApiException(ErrorCode.INVALID_WINDOW, "windowDays should be between 1 to 30");
        }

        if(size <= 0 || size > 50) size = 20;
        if(page < 0) page = 0;

        LocalDateTime from = LocalDateTime.now().minusDays(windowDays);


        return viewEventRepository.findTrendingWithScore(from, LIKE_WEIGHT, PageRequest.of(page, size))
                        .map(p -> new TrendingScoreItemResponse(p.getMovieId(),p.getViewCount(),p.getLikeCount(), p.getSCore()));



    }







}
