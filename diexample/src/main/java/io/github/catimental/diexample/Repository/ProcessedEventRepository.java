package io.github.catimental.diexample.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.catimental.diexample.domain.event.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent,Long>{

    boolean existsByEventId(String eventId);
    
}
