package com.nearkart.repository;

import com.nearkart.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, UUID entityId, Pageable pageable);

    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.action = :action
              AND a.createdAt BETWEEN :from AND :to
            ORDER BY a.createdAt DESC
            """)
    List<AuditLog> findByActionAndDateRange(
            @Param("action") String action,
            @Param("from")   LocalDateTime from,
            @Param("to")     LocalDateTime to);
}
