import org.springframework.data.jpa.repository.JpaRepository;

// compared to respository in member class



import java.util.List;
import java.util.Optional;


public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    boolean existsByMemberIdAndMovieId(Long memberId, Long movieId);

    Optional<Watchlist> findByMemberIdAndMovieId(Long memberId, Long movieId);

    List<Watchlist> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}