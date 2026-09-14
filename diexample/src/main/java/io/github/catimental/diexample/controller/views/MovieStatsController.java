package io.github.catimental.diexample.controller.views;

import org.springframework.web.bind.annotation.RestController;

import io.github.catimental.diexample.Service.Stats.MovieStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.github.catimental.diexample.DTO.Stats.MovieStatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieStatsController {

    private final MovieStatsService movieStatsService;


    @GetMapping("/{movieId}/stats")
    public ResponseEntity<MovieStatsResponse> getMovieStats(@PathVariable Long movieId){
        return ResponseEntity.ok(movieStatsService.getMovieStats(movieId));
    }

    


}
