package io.github.catimental.diexample.publisher;
import io.github.catimental.diexample.DTO.trending.MovieViewedEvent;


public interface MovieEventPublisher {
    void publishMovieViewed(MovieViewedEvent event);
}
