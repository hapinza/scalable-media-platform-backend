package io.github.catimental.diexample.domain.event;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "processed_events",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_consumer", columnNames = {"event_id"})
    }
)
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventId;


    @Column(nullable = false)
    private LocalDateTime processedAt;


    protected ProcessedEvent(){}


    public ProcessedEvent(String eventId){
        this.eventId = eventId;
        this.processedAt = LocalDateTime.now();
    }

    public String getEventId(){
        return this.eventId;
    }

   

    public LocalDateTime getProcessedAt(){
        return this.processedAt;
    }



    
}
