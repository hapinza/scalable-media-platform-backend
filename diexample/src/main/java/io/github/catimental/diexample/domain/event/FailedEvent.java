package io.github.catimental.diexample.domain.event;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;



@Entity
@Table(name = "failed_events")
public class FailedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventId;

    @Lob
    @Column(nullable = false)
    private String payload;


    @Lob
    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime failedAt;

    protected FailedEvent(){}

    public FailedEvent(String eventId, String payload, String reason){
        this.eventId = eventId;
        this.payload = payload;
        this.reason = reason;
        this.failedAt = LocalDateTime.now();
    }

    
    
}
