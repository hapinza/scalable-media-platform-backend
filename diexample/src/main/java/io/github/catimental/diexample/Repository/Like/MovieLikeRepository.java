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
   
    // show the likes in DB (data for front)
    Page<MovieLike> findAllByMember_IdAndLikeTrueOrderByUpdatedAtDesc(Long memberId, Pageable pageable);


    long countByMovieIdAndLikeTrue(Long movieId);


    @Modifying 
    @Query(value = """
                    insert ignore into movie_like (member_id, movie_id, is_like, updated_at)
                    values(:memberId, :movieId, :isLike, now())
                    """, nativeQuery = true)
   int insertIfAbsent(@Param("memberId") Long memberId,
                        @Param("movieId") Long movieId,
                        @Param("isLike") boolean isLike);


   @Modifying 
   @Query(value = """
                   update movie_like
                        set is_like = :isLike, updated_at = now()
                   where member_id = :memberId
                    and movie_id = :movieId
                    and is_like <> :isLike
                   """, nativeQuery = true)
   int updateIfChanged(@Param("memberId") Long memberId,
                        @Param("movieId") Long movieId,
                        @Param("isLike") boolean isLike);

   @Modifying 
   @Query(value = """
                  delete from movie_like where member_id = :memberId and movie_id = :movieId and is_like = true
                   """, nativeQuery = true)
   int deleteIfLiked(@Param("memberId") Long memberId,
                         @Param("movieId") Long movieId);


}


