package io.github.catimental.diexample.DTO.trending;

import java.time.LocalDateTime;

public record MovieViewedEvent(
    String eventId,
    Long memberId,
    Long movieId,
    LocalDateTime occuredAt



) {
    
}
