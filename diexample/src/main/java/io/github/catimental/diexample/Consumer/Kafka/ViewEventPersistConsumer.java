package io.github.catimental.diexample.Consumer.Kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.github.catimental.diexample.DTO.trending.MovieViewedEvent;
import io.github.catimental.diexample.DTO.trending.TrendingItemRequest;
import io.github.catimental.diexample.Repository.*;
import io.github.catimental.diexample.domain.analytics.ViewEvent;
import org.springframework.kafka.annotation.KafkaListener;


@Slf4j
@Service
public class ViewEventPersistConsumer {
    
    private final ViewEventRepository viewEventRepository;

    public ViewEventPersistConsumer(ViewEventRepository viewEventRepository){
        this.viewEventRepository = viewEventRepository;
    }

    //@KafkaListener(topics = "trending-topic", groupId = "view-persist-group")
    public void listen(MovieViewedEvent event){
        viewEventRepository.save(new ViewEvent(event.eventId(), event.memberId(), event.movieId()));
        log.debug("persisted view event movieId={}", event.movieId());
    }


}
