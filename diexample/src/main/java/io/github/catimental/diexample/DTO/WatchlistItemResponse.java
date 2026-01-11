package io.github.catimental.diexample.DTO;
import java.time.LocalDateTime;





public record WatchlistItemResponse (
    Long movieId,
    LocalDateTime createdAt
){}

