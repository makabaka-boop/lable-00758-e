package com.shortvideo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "operation_logs")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;
    private String username;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private Long duration;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
