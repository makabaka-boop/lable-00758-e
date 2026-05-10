package com.shortvideo.service;

import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.*;
import com.shortvideo.exception.BusinessException;
import com.shortvideo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;
    @Mock
    private VideoLikeRepository likeRepository;
    @Mock
    private VideoFavoriteRepository favoriteRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VideoService videoService;

    private Video testVideo;
    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoService, "uploadPath", "/tmp/test-uploads/videos");
        ReflectionTestUtils.setField(videoService, "allowedTypes", "video/mp4,video/webm,video/ogg,video/quicktime");
        ReflectionTestUtils.setField(videoService, "maxSize", 104857600L);

        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setTitle("测试视频");
        testVideo.setDescription("测试描述");
        testVideo.setVideoUrl("/uploads/videos/test.mp4");
        testVideo.setUserId(1L);
        testVideo.setLikeCount(0);
        testVideo.setFavoriteCount(0);
        testVideo.setCommentCount(0);
        testVideo.setViewCount(0);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
    }

    @Test
    void uploadVideo_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.mp4");
        lenient().when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[0]));
        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video v = invocation.getArgument(0);
            v.setId(1L);
            return v;
        });

        Video result = videoService.uploadVideo(file, "测试标题", "测试描述", 1L);

        assertNotNull(result);
        assertEquals("测试标题", result.getTitle());
        assertEquals("测试描述", result.getDescription());
        assertEquals(1L, result.getUserId());
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    void uploadVideo_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, "标题", "描述", 1L));
    }

    @Test
    void uploadVideo_NullFile() {
        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(null, "标题", "描述", 1L));
    }

    @Test
    void uploadVideo_UnsupportedFormat() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("video/avi");

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, "标题", "描述", 1L));
    }

    @Test
    void uploadVideo_NullContentType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn(null);

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, "标题", "描述", 1L));
    }

    @Test
    void uploadVideo_FileTooLarge() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getSize()).thenReturn(200_000_000L);

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, "标题", "描述", 1L));
    }

    @Test
    void uploadVideo_EmptyTitle() {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getContentType()).thenReturn("video/mp4");
        lenient().when(file.getSize()).thenReturn(1024L);

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, "", "描述", 1L));
    }

    @Test
    void uploadVideo_NullTitle() {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getContentType()).thenReturn("video/mp4");
        lenient().when(file.getSize()).thenReturn(1024L);

        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, null, "描述", 1L));
    }

    @Test
    void uploadVideo_TitleTooLong() {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getContentType()).thenReturn("video/mp4");
        lenient().when(file.getSize()).thenReturn(1024L);

        String longTitle = "a".repeat(101);
        assertThrows(BusinessException.class, () ->
                videoService.uploadVideo(file, longTitle, "描述", 1L));
    }

    @Test
    void getVideos_WithUserId() {
        List<Video> videoList = Collections.singletonList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList);
        when(videoRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(videoPage);
        when(likeRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.emptyList());
        when(favoriteRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.emptyList());
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<VideoDTO> result = videoService.getVideos(0, 10, 1L);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("测试视频", result.getContent().get(0).getTitle());
        assertFalse(result.getContent().get(0).getLiked());
        assertFalse(result.getContent().get(0).getFavorited());
        assertEquals("测试用户", result.getContent().get(0).getUsername());
    }

    @Test
    void getVideos_WithoutUserId() {
        List<Video> videoList = Collections.singletonList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList);
        when(videoRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(videoPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<VideoDTO> result = videoService.getVideos(0, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertFalse(result.getContent().get(0).getLiked());
        assertFalse(result.getContent().get(0).getFavorited());
    }

    @Test
    void getVideos_WithLikedAndFavorited() {
        List<Video> videoList = Collections.singletonList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList);
        VideoLike like = new VideoLike();
        like.setVideoId(1L);
        like.setUserId(1L);
        VideoFavorite fav = new VideoFavorite();
        fav.setVideoId(1L);
        fav.setUserId(1L);

        when(videoRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(videoPage);
        when(likeRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.singletonList(like));
        when(favoriteRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.singletonList(fav));
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<VideoDTO> result = videoService.getVideos(0, 10, 1L);

        assertTrue(result.getContent().get(0).getLiked());
        assertTrue(result.getContent().get(0).getFavorited());
    }

    @Test
    void toggleLike_AddLike() {
        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.save(any(VideoLike.class))).thenAnswer(invocation -> {
            VideoLike l = invocation.getArgument(0);
            l.setId(1L);
            return l;
        });

        boolean result = videoService.toggleLike(1L, 1L);

        assertTrue(result);
        verify(likeRepository).save(any(VideoLike.class));
        verify(videoRepository).updateLikeCount(1L, 1);
    }

    @Test
    void toggleLike_RemoveLike() {
        VideoLike existingLike = new VideoLike();
        existingLike.setId(1L);
        existingLike.setUserId(1L);
        existingLike.setVideoId(1L);
        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(existingLike));

        boolean result = videoService.toggleLike(1L, 1L);

        assertFalse(result);
        verify(likeRepository).delete(existingLike);
        verify(videoRepository).updateLikeCount(1L, -1);
    }

    @Test
    void toggleFavorite_AddFavorite() {
        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(VideoFavorite.class))).thenAnswer(invocation -> {
            VideoFavorite f = invocation.getArgument(0);
            f.setId(1L);
            return f;
        });

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertTrue(result);
        verify(favoriteRepository).save(any(VideoFavorite.class));
        verify(videoRepository).updateFavoriteCount(1L, 1);
    }

    @Test
    void toggleFavorite_RemoveFavorite() {
        VideoFavorite existingFav = new VideoFavorite();
        existingFav.setId(1L);
        existingFav.setUserId(1L);
        existingFav.setVideoId(1L);
        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(existingFav));

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertFalse(result);
        verify(favoriteRepository).delete(existingFav);
        verify(videoRepository).updateFavoriteCount(1L, -1);
    }

    @Test
    void addComment_Success() {
        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setVideoId(1L);
        savedComment.setUserId(1L);
        savedComment.setContent("测试评论");
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Comment result = videoService.addComment(1L, 1L, "测试评论");

        assertNotNull(result);
        assertEquals("测试评论", result.getContent());
        assertEquals(1L, result.getVideoId());
        assertEquals(1L, result.getUserId());
        verify(commentRepository).save(any(Comment.class));
        verify(videoRepository).updateCommentCount(1L, 1);
    }

    @Test
    void getComments_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");
        Page<Comment> commentPage = new PageImpl<>(Collections.singletonList(comment));
        when(commentRepository.findByVideoIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(commentPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<Comment> result = videoService.getComments(1L, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("测试用户", result.getContent().get(0).getUsername());
    }

    @Test
    void incrementViewCount() {
        videoService.incrementViewCount(1L);
        verify(videoRepository).incrementViewCount(1L);
    }

    @Test
    void getAllVideos() {
        when(videoRepository.findAll()).thenReturn(Collections.singletonList(testVideo));

        List<Video> result = videoService.getAllVideos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试视频", result.get(0).getTitle());
    }

    @Test
    void deleteVideo() {
        videoService.deleteVideo(1L);
        verify(videoRepository).deleteById(1L);
    }
}
