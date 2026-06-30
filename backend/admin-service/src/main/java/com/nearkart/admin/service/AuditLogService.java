package com.nearkart.admin.service;

import com.nearkart.admin.model.AuditLog;
import com.nearkart.admin.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String adminUsername, String action, String targetEntity, Long targetId, String details) {
        AuditLog log = new AuditLog();
        log.setAdminUsername(adminUsername);
        log.setAction(action);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByAdmin(String adminUsername) {
        return auditLogRepository.findByAdminUsernameOrderByTimestampDesc(adminUsername);
    }

    public List<AuditLog> getLogsByTarget(String targetEntity, Long targetId) {
        return auditLogRepository.findByTargetEntityAndTargetIdOrderByTimestampDesc(targetEntity, targetId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
