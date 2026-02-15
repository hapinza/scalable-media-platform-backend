package io.github.catimental.diexample.Repository;

import org.springframework.stereotype.Repository;
import java.util.Optional;
import io.github.catimental.diexample.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;



@Repository 
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
