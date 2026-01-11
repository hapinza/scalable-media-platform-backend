package io.github.catimental.diexample.DTO.progress;

import java.time.LocalDateTime;

public record ProgressItemResponse (
    Long movieId,
    Integer progressSeconds,
    LocalDateTime updatedAt

) {
    
}
