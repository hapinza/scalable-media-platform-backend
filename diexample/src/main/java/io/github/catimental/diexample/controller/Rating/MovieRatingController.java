package io.github.catimental.diexample.controller.Rating;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.rating.RatingUpsertRequest;
import io.github.catimental.diexample.Service.Rating.MovieRatingService;
import io.github.catimental.diexample.DTO.rating.*;

@RestController
@RequestMapping("/ratings")
public class MovieRatingController {
 
    private final MovieRatingService movieRatingService;

    public MovieRatingController(MovieRatingService movieRatingService){
        this.movieRatingService = movieRatingService;
    }

    @PutMapping("/{movieId")
    public ResponseEntity<Void> upsert(@PathVariable Long movieId, @RequestBody RatingUpsertRequest req, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();

        movieRatingService.upsert(memberId, movieId, req.score());
        return ResponseEntity.ok().build();

    }


    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(@PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();   

        movieRatingService.remove(memberId, movieId);
        return ResponseEntity.ok().build();

    }



    @GetMapping
    public ResponseEntity<Page<RatingUpsertResponse>> list(Page pageable, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();   

        return ResponseEntity.ok(movieRatingService.list(memberId, pageable));


    }


}
