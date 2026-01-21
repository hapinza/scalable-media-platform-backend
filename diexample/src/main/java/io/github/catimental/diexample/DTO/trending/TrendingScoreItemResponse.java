package io.github.catimental.diexample.DTO.trending;

public record TrendingScoreItemResponse (
    Long movieId,
    Long viewCount,
    Long likeCount,
    Long score

){}
