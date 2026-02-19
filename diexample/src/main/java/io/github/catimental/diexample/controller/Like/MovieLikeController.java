package io.github.catimental.diexample.controller.Like;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.Like.LikeUpsertRequest;
import io.github.catimental.diexample.DTO.Like.MovieLikeItemResponse;
import io.github.catimental.diexample.Service.Like.MovieLikeService;
// import io.github.catimental.diexample.domain.like.MovieLike;
import org.springframework.data.domain.Pageable;


import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/likes")
public class MovieLikeController {
    
    private final MovieLikeService movieLikeService;

    public MovieLikeController(MovieLikeService movieLikeService){
        this.movieLikeService = movieLikeService;
    }


    @PutMapping("/{movieId}")
    public ResponseEntity<Void> upsert(@RequestBody LikeUpsertRequest req ,
        @PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();

        movieLikeService.upsert(memberId, movieId, req.like());


        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(@PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();

        movieLikeService.remove(memberId, movieId);

        return ResponseEntity.ok().build();
    }


    @GetMapping
    public ResponseEntity<Page<MovieLikeItemResponse>> list(
        Pageable pageable, Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();


        return ResponseEntity.ok(movieLikeService.likeLists(memberId, pageable));

    }

}
