package com.shortvideo.repository;

import com.shortvideo.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByUserIdAndVideoId(Long userId, Long videoId);
    List<VideoLike> findByUserIdAndVideoIdIn(Long userId, List<Long> videoIds);
}
