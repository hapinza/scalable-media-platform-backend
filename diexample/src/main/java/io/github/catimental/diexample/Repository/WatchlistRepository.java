package io.github.catimental.diexample.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.catimental.diexample.DTO.WatchlistItemResponse;
import io.github.catimental.diexample.domain.watchlist.*;

// compared to respository in member class

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.util.List;
import java.util.Optional;


public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    boolean existsByMemberIdAndMovieId(Long memberId, Long movieId);

    Optional<Watchlist> findByMemberIdAndMovieId(Long memberId, Long movieId);

    List<Watchlist> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);



    @Query(
        """
        select new com.yourpkg.watchlist.WatchlistItemResponse(w.movieId, w.createdAt)
        from Watchlist w
        where w.member.id = :memberId
        order by w.createdAt desc        
                """)
    List<WatchlistItemResponse> findItem(@Param("memberId") Long memberId);



    @Query("""
        select w from Watchlist w
        join fetch w.member
        where w.member.id = :memberId
        order by w.createdAt desc
    """)
    // to prevent JPA N+1  (proxy)
    List<Watchlist> findAllWithMember(@Param("memberId")Long memberId);

}