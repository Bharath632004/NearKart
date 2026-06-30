package com.nearkart.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminUsername;

    @Column(nullable = false)
    private String action;        // e.g. BAN_USER, APPROVE_MERCHANT, REJECT_MERCHANT

    private String targetEntity;  // e.g. USER, MERCHANT, ORDER

    private Long targetId;

    private String details;       // extra context/notes

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
