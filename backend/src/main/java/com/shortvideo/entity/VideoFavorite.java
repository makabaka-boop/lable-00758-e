package com.shortvideo.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "video_favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class VideoFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "video_id", nullable = false)
    private Long videoId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
