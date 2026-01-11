package io.github.catimental.diexample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.progress.ProgressItemResponse;
import io.github.catimental.diexample.DTO.progress.ProgressUpSertRequest;
import io.github.catimental.diexample.Service.WatchingProgressService;

import java.util.List;


@RestController
@RequestMapping("/progress")
public class WatchingProgressController {
    private final WatchingProgressService watchingProgressService;


    public WatchingProgressController(WatchingProgressService watchingProgressService){
        this.watchingProgressService = watchingProgressService;
    }


    //store/ update
    @PutMapping("/{movieId}")
    public ResponseEntity<Void> upsert(@PathVariable Long movieId, 
                                    @RequestBody ProgressUpSertRequest req, 
                                    Authentication authentication){
    Long memberId = (Long) authentication.getPrincipal();
    watchingProgressService.upsert(memberId, movieId, req.porgressSeconds());
    return ResponseEntity.ok().build();
    
    
    
    }


    @GetMapping
    public ResponseEntity<List<ProgressItemResponse>> list(Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();
        
        return ResponseEntity.ok(watchingProgressService.list(memberId));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(@PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();
        watchingProgressService.remove(memberId, movieId);

        return ResponseEntity.ok().build();
    }


}
