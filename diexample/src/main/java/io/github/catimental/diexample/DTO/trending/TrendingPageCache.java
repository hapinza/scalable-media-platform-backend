package io.github.catimental.diexample.DTO.trending;

import java.util.List;


public record TrendingPageCache<T>(
    List<T> items,
    int number,
    int size,
    long totalElements, 
    boolean hasNext
){}
