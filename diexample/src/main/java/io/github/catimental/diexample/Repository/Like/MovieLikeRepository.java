package io.github.catimental.diexample.Repository.Like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import io.github.catimental.diexample.domain.like.MovieLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.repository.query.Param;



public interface MovieLikeRepository extends JpaRepository<MovieLike, Long>{
    // add like in DB (server)
    Optional<MovieLike> findByMemberIdAndMovieId(Long memberId, Long movieId);

    // show the likes in DB (data for front)
    Page<MovieLike> findAllByMember_IdAndLikeTrueOrderByUpdatedAtDesc(Long memberId, Pageable pageable);


    long countByMovieIdAndLikeTrue(Long movieId);

    @Modifying
    @Query(value = """
            insert into movie_like (member_id, movie_id, is_like, updated_at)
            values (:memberId, :movieId, :isLike, now())
            on duplicate key update 
               is_like = values(is_like),
               updated_at = values(updated_at)
            """, nativeQuery = true)
    int upsertLike(@Param("memberId") Long memberId,
                    @Param("movieId") Long movieId,
                    @Param("isLike") boolean isLike);

}


