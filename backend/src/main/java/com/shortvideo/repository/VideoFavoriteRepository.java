package com.shortvideo.repository;

import com.shortvideo.entity.VideoFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VideoFavoriteRepository extends JpaRepository<VideoFavorite, Long> {
    Optional<VideoFavorite> findByUserIdAndVideoId(Long userId, Long videoId);
    List<VideoFavorite> findByUserIdAndVideoIdIn(Long userId, List<Long> videoIds);
}
