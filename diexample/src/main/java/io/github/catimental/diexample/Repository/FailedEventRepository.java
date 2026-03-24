package io.github.catimental.diexample.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import io.github.catimental.diexample.domain.event.FailedEvent;

public interface FailedEventRepository extends JpaRepository<FailedEvent, Long>{
}
