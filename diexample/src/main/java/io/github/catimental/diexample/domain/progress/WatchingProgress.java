package io.github.catimental.diexample.domain.progress;

import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import io.github.catimental.diexample.domain.*;


import java.time.LocalDateTime;


@Entity
@Table(
    name = "watching_progress",
    uniqueConstraints = @UniqueConstraint(name = "uk_progress_member_movie", columnNames = {
            "member_id", "movie_id"
        }),
    indexes = @Index(name = "idx_progress_member_updated", columnList = "member_id,updated_at")
)



public class WatchingProgress {
    
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;



@ManyToOne(fetch= FetchType.LAZY, optional = false)
@JoinColumn(name = "member_id", nullable = false)
private Member member;



@Column(name = "movie_id", nullable = false)
private Long movieId;

@Column(name = "progress_second", nullable= false)
private Integer progressSeconds;

@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

protected WatchingProgress(){};


public WatchingProgress(Member member, Long movieId, Integer progressSeconds){
    this.member = member;
    this.movieId = movieId;
    this.progressSeconds = progressSeconds;
}

public void updateProgress(Integer progressSeconds){
    this.progressSeconds = progressSeconds;
    this.updatedAt = LocalDateTime.now();
}

public Long getId(){ return this.id;}
public Member getMember() { return this.member; }
public Long getMovieId() {return this.movieId; }
public Integer getProgressSeconds(){return this.progressSeconds;}
public LocalDateTime getUpdatedAt(){return this.updatedAt;}


}
