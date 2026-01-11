package io.github.catimental.diexample.domain.watchlist;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import io.github.catimental.diexample.domain.*;

@Entity
@Table(
    name = "watchlist",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_watchlist_member_movie", columnNames = {"member_id", "movie_id"})
    }

)
public class Watchlist {
    
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

private Long id;


@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "member_id", nullable = false)
private Member member;



@Column(name = "movie_id", nullable = false)
private Long movieId;


@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;


protected Watchlist(){}

public Watchlist(Member member, Long movieId){
    this.member = member;
    this.movieId = movieId;
    this.createdAt = LocalDateTime.now();
}


public Long getId(){ return id; }
public Member getMember(){ return member; }
public Long getMovieId() { return movieId; }
public LocalDateTime getCreatedAt() { return createdAt; }
 








}
