package io.github.catimental.diexample.controller.views;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.trending.TrendingScoreItemResponse;
import io.github.catimental.diexample.Service.*;


@RestController
public class AnalyticsController {
    private final ViewEventService viewEventService;
    private final TrendingService trendingService;


    public AnalyticsController(ViewEventService viewEventService, TrendingService trendingService){
        this.viewEventService = viewEventService;
        this.trendingService = trendingService;
    }


    @PostMapping("/analytics/views/{movieId}")
    public ResponseEntity<Void> trackView(@PathVariable Long movieId, Authentication authentication){
        // doesn't have to be member with not null
        // can be anonymous
        // therefore , should permit all in sercurityconfig
        Long memberId = null;
        if(authentication != null && authentication.getPrincipal() != null){
            if(authentication.getPrincipal() instanceof Long pid) memberId = pid;
        }
        viewEventService.track(memberId, movieId);
        // we wait until viewEvent stores the data. However, this takes time quite a lot
        // instead we immediatelt return with Kafka
        return ResponseEntity.ok().build();
    }




    // output the result of movie, viewCounts, like, score 
    @GetMapping("/movies/trending")
    public ResponseEntity<Page<TrendingScoreItemResponse>> trending(
        @RequestParam(defaultValue = "7") int windowDays,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ){
        return ResponseEntity.ok(trendingService.trendingWithScore(windowDays, page, size));
    }





}
