package io.github.catimental.diexample.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.catimental.diexample.DTO.trending.TrendingItemRequest;
import io.github.catimental.diexample.Repository.ViewEventRepository;

// kafka 
// import org.springframework.kafka.core.KafkaTemplate;



@Service
@Transactional
public class ViewEventService {
    private final ViewEventRepository viewEventRepository;


    @Autowired
    private KafkaTemplate<String, TrendingItemRequest> kafkaTemplate;
    //kafka for receiving data, not heavy data
    /*
    trending-topic (name of kafka) 
    ├─ partition-0 
    ├─ partition-1 
    ├─ partition-2
    
    
    (Parallelism)
    for each partition, consumer gets logic, processes it, store either in Redis or DB.
    
    kafka is used for asynchronous response when user request something. but whether we 
    would use kafka depends on if the request involves Event/Core User State/Immediate 
    consistency

    ex) Like
    1) a User likes movie(Core User State)
    2) a movie is liked the most/least by user(Event)

    ex) sign in/ payment
    these are for immediate consistency
    */
   public void sendTrendingDataToKafka(String movieId, TrendingItemRequest trendingItemRequest){
    kafkaTemplate.send("trending-topic", movieId, trendingItemRequest);
   }


    public ViewEventService(ViewEventRepository viewEventRepository){
        this.viewEventRepository = viewEventRepository;
    }


    public void track(Long memberIdorNull, Long movieId){
       // viewEventRepository.save(new ViewEvent(memberIdorNull, movieId));
        TrendingItemRequest event = new TrendingItemRequest(
            memberIdorNull,
            movieId
        );


        kafkaTemplate.send("trending-topic",  String.valueOf(movieId), event);
        // duplicate of movieId twice because kafka uses the key . value
        // key(movieId) is for identifying key 
        // value(data) is for consumer to store the data through logic
        

    }
}
