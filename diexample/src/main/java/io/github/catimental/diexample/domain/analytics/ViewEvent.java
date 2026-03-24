package io.github.catimental.diexample.domain.analytics;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "view_event", indexes ={
    @Index(name = "idx_view_movie_created", columnList = "movie_id,created_at"),
    @Index(name = "idx_view_member_created", columnList = "member_id,created_at")

},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_view_event_event_id", columnNames = "event_id")
}

)



// in ViewEvent, time(created_at) is the most significant variable. 
// compared to the other DB such as watchingprogress(relationship) 
// which refers to member.
public class ViewEvent {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;


    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ViewEvent(){}

    public ViewEvent(String eventId, Long memberId, Long movieId){
        this.eventId = eventId;
        this.memberId = memberId;
        this.movieId = movieId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getMovieId() { return this.movieId; }
    public LocalDateTime getCreatedAt(){return this.createdAt;}

}
