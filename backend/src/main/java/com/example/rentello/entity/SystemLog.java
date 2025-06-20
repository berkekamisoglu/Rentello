package com.example.rentello.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "SystemLogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "logId")
@EntityListeners(AuditingEntityListener.class)
public class SystemLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;
    
    @Column(name = "TableName", nullable = false, length = 100)
    private String tableName;
    
    @Column(name = "OperationType", nullable = false, length = 20)
    private String operationType; // INSERT, UPDATE, DELETE
    
    @Column(name = "RecordID")
    private Integer recordId;
    
    @Column(name = "OldValues", columnDefinition = "NVARCHAR(MAX)")
    private String oldValues; // JSON format
    
    @Column(name = "NewValues", columnDefinition = "NVARCHAR(MAX)")
    private String newValues; // JSON format
    
    @Column(name = "LogDate")
    private LocalDateTime logDate = LocalDateTime.now();
    
    @Column(name = "IPAddress", length = 45)
    private String ipAddress;
    
    @Column(name = "UserAgent", length = 500)
    private String userAgent;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID")
    private User user;
}
