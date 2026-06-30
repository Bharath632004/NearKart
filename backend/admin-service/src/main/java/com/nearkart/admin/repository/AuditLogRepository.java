package com.nearkart.admin.repository;

import com.nearkart.admin.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByAdminUsernameOrderByTimestampDesc(String adminUsername);
    List<AuditLog> findByTargetEntityAndTargetIdOrderByTimestampDesc(String targetEntity, Long targetId);
}
