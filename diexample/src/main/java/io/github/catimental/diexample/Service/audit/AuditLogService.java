package io.github.catimental.diexample.Service.audit;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import io.github.catimental.diexample.Repository.Audit.AuditLogRepository;
import io.github.catimental.diexample.domain.audit.*;

@Service
public abstract class AuditLogService {
    private final AuditLogRepository auditLogRepository;


    public AuditLogService(AuditLogRepository auditLogRepository){
        this.auditLogRepository = auditLogRepository;
    }


    @Transactional
    public void log(Long memberId, String action, boolean success, String detail, String ip, String userAgent){
        try{
            auditLogRepository.save(new AuditLog(memberId, action, success, detail, ip, userAgent));
        }catch(Exception ignored){
            // event log fail doesn't block main function
        }
    }



}
