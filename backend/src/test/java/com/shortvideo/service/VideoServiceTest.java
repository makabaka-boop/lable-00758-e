package com.shortvideo.service;

import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.*;
import com.shortvideo.exception.BusinessException;
import com.shortvideo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoService 单元测试")
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
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

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
        testVideo.setCreatedAt(LocalDateTime.now());

        ReflectionTestUtils.setField(videoService, "uploadPath", "/tmp/test-uploads");
        ReflectionTestUtils.setField(videoService, "allowedTypes", "video/mp4,video/webm,video/ogg");
        ReflectionTestUtils.setField(videoService, "maxSize", 10485760L);
    }

    @Test
    @DisplayName("上传视频成功")
    void uploadVideo_Success() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video content".getBytes()
        );

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video savedVideo = invocation.getArgument(0);
            savedVideo.setId(1L);
            return savedVideo;
        });

        Video result = videoService.uploadVideo(file, "测试视频", "测试描述", 1L);

        assertNotNull(result);
        assertEquals("测试视频", result.getTitle());
        assertEquals("测试描述", result.getDescription());
        assertEquals(1L, result.getUserId());
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频失败 - 文件为空")
    void uploadVideo_EmptyFile() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(null, "测试视频", "测试描述", 1L);
        });

        assertEquals("请选择要上传的视频文件", exception.getMessage());
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频失败 - 不支持的格式")
    void uploadVideo_UnsupportedFormat() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "测试视频", "测试描述", 1L);
        });

        assertEquals("不支持的视频格式，仅支持 MP4/WebM/OGG", exception.getMessage());
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频失败 - 标题为空")
    void uploadVideo_EmptyTitle() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video content".getBytes()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "", "测试描述", 1L);
        });

        assertEquals("视频标题不能为空", exception.getMessage());
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频失败 - 标题过长")
    void uploadVideo_TitleTooLong() {
        MultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test video content".getBytes()
        );

        String longTitle = "a".repeat(101);
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, longTitle, "测试描述", 1L);
        });

        assertEquals("视频标题不能超过100个字符", exception.getMessage());
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    @DisplayName("获取视频列表成功")
    void getVideos_Success() {
        List<Video> videoList = Collections.singletonList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList, PageRequest.of(0, 10), 1);

        when(videoRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(videoPage);
        when(likeRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.emptyList());
        when(favoriteRepository.findByUserIdAndVideoIdIn(eq(1L), anyList())).thenReturn(Collections.emptyList());
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<VideoDTO> result = videoService.getVideos(0, 10, 1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试视频", result.getContent().get(0).getTitle());
        assertEquals("测试用户", result.getContent().get(0).getUsername());
        verify(videoRepository).findAllByOrderByCreatedAtDesc(any(PageRequest.class));
    }

    @Test
    @DisplayName("点赞视频 - 首次点赞")
    void toggleLike_FirstLike() {
        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = videoService.toggleLike(1L, 1L);

        assertTrue(result);
        verify(likeRepository).save(any(VideoLike.class));
        verify(videoRepository).updateLikeCount(1L, 1);
    }

    @Test
    @DisplayName("点赞视频 - 取消点赞")
    void toggleLike_CancelLike() {
        VideoLike videoLike = new VideoLike();
        videoLike.setId(1L);
        videoLike.setUserId(1L);
        videoLike.setVideoId(1L);

        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(videoLike));

        boolean result = videoService.toggleLike(1L, 1L);

        assertFalse(result);
        verify(likeRepository).delete(videoLike);
        verify(videoRepository).updateLikeCount(1L, -1);
    }

    @Test
    @DisplayName("收藏视频 - 首次收藏")
    void toggleFavorite_FirstFavorite() {
        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertTrue(result);
        verify(favoriteRepository).save(any(VideoFavorite.class));
        verify(videoRepository).updateFavoriteCount(1L, 1);
    }

    @Test
    @DisplayName("收藏视频 - 取消收藏")
    void toggleFavorite_CancelFavorite() {
        VideoFavorite videoFavorite = new VideoFavorite();
        videoFavorite.setId(1L);
        videoFavorite.setUserId(1L);
        videoFavorite.setVideoId(1L);

        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(videoFavorite));

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertFalse(result);
        verify(favoriteRepository).delete(videoFavorite);
        verify(videoRepository).updateFavoriteCount(1L, -1);
    }

    @Test
    @DisplayName("添加评论成功")
    void addComment_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = videoService.addComment(1L, 1L, "测试评论");

        assertNotNull(result);
        assertEquals("测试评论", result.getContent());
        verify(commentRepository).save(any(Comment.class));
        verify(videoRepository).updateCommentCount(1L, 1);
    }

    @Test
    @DisplayName("获取评论列表成功")
    void getComments_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");
        comment.setCreatedAt(LocalDateTime.now());

        List<Comment> commentList = Collections.singletonList(comment);
        Page<Comment> commentPage = new PageImpl<>(commentList, PageRequest.of(0, 20), 1);

        when(commentRepository.findByVideoIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(commentPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<Comment> result = videoService.getComments(1L, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("测试用户", result.getContent().get(0).getUsername());
        verify(commentRepository).findByVideoIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class));
    }

    @Test
    @DisplayName("增加观看次数")
    void incrementViewCount_Success() {
        doNothing().when(videoRepository).incrementViewCount(1L);

        videoService.incrementViewCount(1L);

        verify(videoRepository).incrementViewCount(1L);
    }

    @Test
    @DisplayName("获取所有视频 - 管理员")
    void getAllVideos_Success() {
        List<Video> videoList = Collections.singletonList(testVideo);
        when(videoRepository.findAll()).thenReturn(videoList);

        List<Video> result = videoService.getAllVideos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试视频", result.get(0).getTitle());
        verify(videoRepository).findAll();
    }

    @Test
    @DisplayName("删除视频成功")
    void deleteVideo_Success() {
        doNothing().when(videoRepository).deleteById(1L);

        videoService.deleteVideo(1L);

        verify(videoRepository).deleteById(1L);
    }
}
