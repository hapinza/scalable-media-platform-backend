package io.github.catimental.diexample.Repository;

import org.springframework.stereotype.Repository;


@Repository 
public class MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
}
