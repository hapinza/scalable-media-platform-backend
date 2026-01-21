package io.github.catimental.diexample.Repository.Like;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import io.github.catimental.diexample.domain.like.MovieLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;



public interface MovieLikeRepository extends JpaRepository<MovieLike, Long>{
    // add like in DB (server)
    Optional<MovieLike> findByMemberIdAndMovieId(Long memberId, Long movieId);

    // show the likes in DB (data for front)
    Page<MovieLike> findAllByMemebrIdAndLikeTrueOrderByUpdatedAtDesc(Long memberId, Page pageable);


}


