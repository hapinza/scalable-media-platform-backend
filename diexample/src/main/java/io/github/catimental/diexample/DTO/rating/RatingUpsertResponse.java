package io.github.catimental.diexample.DTO.rating;

import java.time.LocalDateTime;

public record RatingUpsertResponse (
    Long movieId,
    int score,
    LocalDateTime updatedAt
){}

