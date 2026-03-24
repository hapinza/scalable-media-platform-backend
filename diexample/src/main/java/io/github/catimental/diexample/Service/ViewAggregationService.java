package io.github.catimental.diexample.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import io.github.catimental.diexample.Repository.ViewEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import io.github.catimental.diexample.domain.analytics.ViewEvent;


@Service
@RequiredArgsConstructor
public class ViewAggregationService {
    private final ViewEventRepository viewEventRepository;


    @Transactional
    public void processedViewEvent(String eventId, Long memberId, Long movieId){
        ViewEvent event = new ViewEvent(eventId, memberId, movieId);
        viewEventRepository.save(event);
    }




}
