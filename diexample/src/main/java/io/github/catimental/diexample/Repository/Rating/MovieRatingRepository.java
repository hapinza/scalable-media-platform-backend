package io.github.catimental.diexample.Repository.Rating;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import io.github.catimental.diexample.domain.rating.MovieRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MovieRatingRepository extends JpaRepository<MovieRating, Long>{

    Optional<MovieRating> findByMemberIdAndMovieId(Long memberId, Long mvoieId);
    
    Page<MovieRating> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId, Page Pageable);
    
}
