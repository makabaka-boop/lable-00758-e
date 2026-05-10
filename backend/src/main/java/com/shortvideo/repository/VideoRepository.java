package com.shortvideo.repository;

import com.shortvideo.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    @Modifying
    @Query("UPDATE Video v SET v.likeCount = v.likeCount + ?2 WHERE v.id = ?1")
    void updateLikeCount(Long videoId, int delta);
    
    @Modifying
    @Query("UPDATE Video v SET v.favoriteCount = v.favoriteCount + ?2 WHERE v.id = ?1")
    void updateFavoriteCount(Long videoId, int delta);
    
    @Modifying
    @Query("UPDATE Video v SET v.commentCount = v.commentCount + ?2 WHERE v.id = ?1")
    void updateCommentCount(Long videoId, int delta);
    
    @Modifying
    @Query("UPDATE Video v SET v.viewCount = v.viewCount + 1 WHERE v.id = ?1")
    void incrementViewCount(Long videoId);
}
