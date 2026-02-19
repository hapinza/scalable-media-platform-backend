package io.github.catimental.diexample.Repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import io.github.catimental.diexample.DTO.progress.ProgressItemResponse;


import io.github.catimental.diexample.domain.progress.*;


public interface WatchingProgressRepository extends JpaRepository<WatchingProgress, Long>{



    Optional<WatchingProgress> findByMemberIdAndMovieId(Long memberId, Long movieId);
    List<WatchingProgress> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId);


    @Query("""
        select new com.yourpkg.progress.progressItemResponse(p.movieId, p.progressSeconds, p.updatedAt)
        from WatchingProgress p
        where p.member.id = :memberId
        order by p.updatedAt desc
        """)
        List<ProgressItemResponse> findItems(@Param("memberId") Long memberId);
        


}

