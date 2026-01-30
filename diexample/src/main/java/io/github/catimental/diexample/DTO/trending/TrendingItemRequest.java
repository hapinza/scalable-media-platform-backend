package io.github.catimental.diexample.DTO.trending;

import java.time.LocalDateTime;

public record TrendingItemRequest (
    Long memberId,
    Long movieId
){}
