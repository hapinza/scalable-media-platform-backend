package io.github.catimental.diexample.Repository.Audit;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.catimental.diexample.domain.audit.AuditLog;


public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {




}
