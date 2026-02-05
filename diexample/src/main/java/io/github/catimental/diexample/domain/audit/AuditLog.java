package io.github.catimental.diexample.domain.audit;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table( name = "audit_log", indexes = {
    @Index(name = "idx_audit_member_created", columnList = "member_id, created_at"),
    @Index(name = "idx_audit_action_created", columnList = "action, created_at")
}   
)

public class AuditLog {


@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


@Column(name = "member_id")
private Long memberId;  // including action with not logined

@Column(name = "action", nullable = false, length = 50)
private String action; // ex. LOGIN_SUCCESS , LOGIN_FAIL, REFRSH, LOGOUT



@Column(name = "success", nullable = false)
private boolean success;


@Column(name = "detail", length = 50)
private String detail;

@Column(name = "ip", length = 45)
private String ip;

@Column(name = "user_agent", length = 200)
private String userAgent;

@Column(name = "created_at", nullable = false , updatable = false)
private LocalDateTime createdAt;


public AuditLog(){}


public AuditLog(Long memberId, String action, boolean success, String detail, String ip, String userAgent){
    this.memberId = memberId;
    this.action = action;
    this.success = success;
    this.detail = detail;
    this.ip = ip;
    this.userAgent = userAgent;
    this.createdAt = LocalDateTime.now();
}



    
}
