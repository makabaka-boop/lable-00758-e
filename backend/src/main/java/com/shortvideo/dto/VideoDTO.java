package com.shortvideo.dto;

import com.shortvideo.entity.Video;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoDTO {
    private Long id;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Long userId;
    private String username;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer viewCount;
    private Boolean liked;
    private Boolean favorited;
    private LocalDateTime createdAt;

    public static VideoDTO fromEntity(Video video) {
        VideoDTO dto = new VideoDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setVideoUrl(video.getVideoUrl());
        dto.setCoverUrl(video.getCoverUrl());
        dto.setUserId(video.getUserId());
        dto.setLikeCount(video.getLikeCount());
        dto.setFavoriteCount(video.getFavoriteCount());
        dto.setCommentCount(video.getCommentCount());
        dto.setViewCount(video.getViewCount());
        dto.setCreatedAt(video.getCreatedAt());
        dto.setLiked(false);
        dto.setFavorited(false);
        return dto;
    }
}
