package com.shortvideo.service;

import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.*;
import com.shortvideo.exception.BusinessException;
import com.shortvideo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    @Autowired
    private VideoRepository videoRepository;
    @Autowired
    private VideoLikeRepository likeRepository;
    @Autowired
    private VideoFavoriteRepository favoriteRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${video.upload-path}")
    private String uploadPath;

    @Value("${video.allowed-types:video/mp4,video/webm,video/ogg}")
    private String allowedTypes;

    @Value("${video.max-size:104857600}")
    private long maxSize;

    public Video uploadVideo(MultipartFile file, String title, String description, Long userId) throws IOException {
        // 文件校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的视频文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(allowedTypes.split(",")).contains(contentType)) {
            throw new BusinessException("不支持的视频格式，仅支持 MP4/WebM/OGG");
        }
        
        if (file.getSize() > maxSize) {
            throw new BusinessException("视频文件过大，最大支持100MB");
        }

        // 标题校验
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("视频标题不能为空");
        }
        if (title.length() > 100) {
            throw new BusinessException("视频标题不能超过100个字符");
        }

        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        // 安全的文件名
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + extension;
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        log.info("视频上传成功: userId={}, fileName={}, size={}", userId, fileName, file.getSize());

        Video video = new Video();
        video.setTitle(title.trim());
        video.setDescription(description != null ? description.trim() : "");
        video.setVideoUrl("/uploads/videos/" + fileName);
        video.setUserId(userId);
        return videoRepository.save(video);
    }

    public Page<VideoDTO> getVideos(int page, int size, Long userId) {
        Page<Video> videos = videoRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        List<Long> videoIds = videos.getContent().stream().map(Video::getId).collect(Collectors.toList());

        Set<Long> likedIds = new HashSet<>();
        Set<Long> favoritedIds = new HashSet<>();
        if (userId != null) {
            likedIds = likeRepository.findByUserIdAndVideoIdIn(userId, videoIds)
                    .stream().map(VideoLike::getVideoId).collect(Collectors.toSet());
            favoritedIds = favoriteRepository.findByUserIdAndVideoIdIn(userId, videoIds)
                    .stream().map(VideoFavorite::getVideoId).collect(Collectors.toSet());
        }

        Map<Long, String> userNames = new HashMap<>();
        Set<Long> userIds = videos.getContent().stream().map(Video::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        userRepository.findAllById(userIds).forEach(u -> userNames.put(u.getId(), u.getNickname()));

        Set<Long> finalLikedIds = likedIds;
        Set<Long> finalFavoritedIds = favoritedIds;
        return videos.map(v -> {
            VideoDTO dto = VideoDTO.fromEntity(v);
            dto.setLiked(finalLikedIds.contains(v.getId()));
            dto.setFavorited(finalFavoritedIds.contains(v.getId()));
            dto.setUsername(userNames.get(v.getUserId()));
            return dto;
        });
    }

    @Transactional
    public boolean toggleLike(Long videoId, Long userId) {
        Optional<VideoLike> existing = likeRepository.findByUserIdAndVideoId(userId, videoId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            videoRepository.updateLikeCount(videoId, -1);
            return false;
        } else {
            VideoLike like = new VideoLike();
            like.setUserId(userId);
            like.setVideoId(videoId);
            likeRepository.save(like);
            videoRepository.updateLikeCount(videoId, 1);
            return true;
        }
    }

    @Transactional
    public boolean toggleFavorite(Long videoId, Long userId) {
        Optional<VideoFavorite> existing = favoriteRepository.findByUserIdAndVideoId(userId, videoId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            videoRepository.updateFavoriteCount(videoId, -1);
            return false;
        } else {
            VideoFavorite fav = new VideoFavorite();
            fav.setUserId(userId);
            fav.setVideoId(videoId);
            favoriteRepository.save(fav);
            videoRepository.updateFavoriteCount(videoId, 1);
            return true;
        }
    }

    @Transactional
    public Comment addComment(Long videoId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);
        videoRepository.updateCommentCount(videoId, 1);
        return saved;
    }

    public Page<Comment> getComments(Long videoId, int page, int size) {
        Page<Comment> comments = commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, PageRequest.of(page, size));
        Set<Long> userIds = comments.getContent().stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getId(), u));
        comments.forEach(c -> {
            User u = userMap.get(c.getUserId());
            if (u != null) {
                c.setUsername(u.getNickname());
                c.setAvatar(u.getAvatar());
            }
        });
        return comments;
    }

    @Transactional
    public void incrementViewCount(Long videoId) {
        videoRepository.incrementViewCount(videoId);
    }

    public List<Video> getAllVideos() {
        return videoRepository.findAll();
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }
}
