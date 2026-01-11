package io.github.catimental.diexample.Repository;


import java.util.List;
import java.util.Optional;

import io.github.catimental.diexample.domain.progress.*;


public class WatchingProgressRepository extends JpaRepository<WatchingProgress, Long>{
    Optional<WatchingProgress> findByMemberIdAndMovieId(Long memberId, Long movieId);
    List<WatchingProgress> findAllByMemberIdOrderByUpdatedAtDesc(Long memberId);
}

