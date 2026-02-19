package io.github.catimental.diexample.Service.Consumer;

//import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import io.github.catimental.diexample.DTO.trending.TrendingItemRequest;
import io.github.catimental.diexample.Repository.ViewEventRepository;
import io.github.catimental.diexample.domain.analytics.ViewEvent;
import lombok.extern.slf4j.Slf4j;  // dependency

@Slf4j
@Service
//@EnableKafka
public class TrendingConsumer {

    private final ViewEventRepository viewEventRepository;

    public TrendingConsumer(ViewEventRepository viewEventRepository){
        this.viewEventRepository = viewEventRepository;
    }


    // with kafkaTemplate.send
    @KafkaListener(topics = "trending-topic", groupId = "test-group")
    // if the group differs, consumer processes the same request at the same time
    // but when it is the same, only will take one task. 
    public void listen(TrendingItemRequest event){
        log.info("Received trending data: {}" , event);

        viewEventRepository.save(new ViewEvent(event.memberId(), event.movieId()));
    }





}
