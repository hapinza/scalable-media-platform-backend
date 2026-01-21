package io.github.catimental.diexample.DTO.Like;

import java.time.LocalDateTime;

public record MovieLikeItemResponse (
    Long movieId,
    boolean like,
    LocalDateTime updatedAt
){ }
