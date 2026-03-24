package io.github.catimental.diexample.domain.rating;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import io.github.catimental.diexample.domain.Member;


@Entity
@Table(
    // member, movieId, score 
    // relationship DB , not Event DB
    name = "movie_rating",
    uniqueConstraints =  {
        @UniqueConstraint(name = "uk_rating_member_movie", columnNames = {"member_id", "movie_id"})

    },
    indexes = {
        @Index(name = "idx_rating_movie", columnList = "movie_id"),
        @Index(name ="idx_rating_memer", columnList = "member_id")
    }


)
public class MovieRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Column(name = "movie_id", nullable = false)
    private Long movieId;


    @Column(name = "socre", nullable = false)
    private int score;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;


    protected MovieRating(){}

    public MovieRating(Member member, Long movieId, int score){
        this.member = member;
        this.movieId = movieId;
        this.score = score;
        this.updatedAt = LocalDateTime.now();
    }


    public void updateScore(int score){
        this.score = score;
    }

    public Long getMovieId(){return this.movieId; }
    public int getScore(){return this.score; }
    public LocalDateTime getUpdatedAt(){return this.getUpdatedAt(); }

    

    
}
