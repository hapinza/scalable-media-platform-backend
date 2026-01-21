package io.github.catimental.diexample.domain.like;


import jakarta.persistence.*;
import java.time.LocalDateTime;

import io.github.catimental.diexample.domain.Member;

@Entity
@Table(
    name = "movie_like",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_like_member_movie", columnNames = {"member_id", "movie_id"})

    },
    indexes = {
        @Index(name = "idx_like_movie", columnList = "movie_id"),
        @Index(name = "idx_like_member", columnList = "member_id")
    }
)
public class MovieLike {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "is_like", nullable = false)
    private boolean like;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    protected MovieLike(){}

    public MovieLike(Member member, Long movieId, boolean like){
        this.member = member;
        this.movieId = movieId;
        this.like = like;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getMovieId(){ return this.movieId; }
    public boolean isLike(){return this.like; }





}
