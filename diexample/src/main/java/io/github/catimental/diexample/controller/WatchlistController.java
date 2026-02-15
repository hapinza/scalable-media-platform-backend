package io.github.catimental.diexample.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.github.catimental.diexample.DTO.WatchlistItemResponse;
import io.github.catimental.diexample.Service.WatchlistService;

import java.util.List;


@RestController
@RequestMapping("/watchlist")
public class WatchlistController {
    
    private final WatchlistService watchlistService;
    
    
    public WatchlistController(WatchlistService watchlistService){
        this.watchlistService = watchlistService;
    }
    
    @PostMapping("/{movieId}")
    public ResponseEntity<Void> add(@PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();
        watchlistService.add(memberId, movieId);
        return ResponseEntity.ok().build();
    
    }
    
    
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> remove(@PathVariable Long movieId, Authentication authentication){
        Long memberId = (Long) authentication.getPrincipal();
        watchlistService.remove(memberId, movieId);
        return ResponseEntity.ok().build();
    }
    
    
    @GetMapping
    public ResponseEntity<List<WatchlistItemResponse>> list(Authentication authentication
        // @RequestParam(defaultValue = "0") int page,
        // @RequestParam(defaultValue = "20") int size
    ){
         Long memberId = (Long) authentication.getPrincipal();
         return ResponseEntity.ok(watchlistService.list(memberId));    
    }
    
    
    
    
    }
    