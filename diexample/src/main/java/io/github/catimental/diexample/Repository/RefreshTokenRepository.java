package io.github.catimental.diexample.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.catimental.diexample.domain.Auth.*;;

import java.util.Optional;

public class RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByTokenHash(String tokenHash);

}
