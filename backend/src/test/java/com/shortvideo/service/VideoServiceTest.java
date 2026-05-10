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

import java.io.IOException;
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

        ReflectionTestUtils.setField(videoService, "uploadPath", "/tmp/test-uploads");
        ReflectionTestUtils.setField(videoService, "allowedTypes", "video/mp4,video/webm,video/ogg");
        ReflectionTestUtils.setField(videoService, "maxSize", 10485760L);
    }

    @Test
    @DisplayName("上传视频 - 成功")
    void uploadVideo_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Video result = videoService.uploadVideo(file, "测试视频标题", "测试描述", 1L);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("测试视频标题", result.getTitle());
        assertEquals("测试描述", result.getDescription());
        assertEquals(1L, result.getUserId());
        assertNotNull(result.getVideoUrl());
        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频 - 失败：文件为空")
    void uploadVideo_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "video/mp4",
                new byte[0]
        );

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "测试标题", "描述", 1L);
        });
        verify(videoRepository, never()).save(any(Video.class));
    }

    @Test
    @DisplayName("上传视频 - 失败：文件为空对象")
    void uploadVideo_NullFile() {
        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(null, "测试标题", "描述", 1L);
        });
    }

    @Test
    @DisplayName("上传视频 - 失败：不支持的文件类型")
    void uploadVideo_UnsupportedType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not a video".getBytes()
        );

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "测试标题", "描述", 1L);
        });
    }

    @Test
    @DisplayName("上传视频 - 失败：文件过大")
    void uploadVideo_FileTooLarge() {
        byte[] largeContent = new byte[10485761];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.mp4",
                "video/mp4",
                largeContent
        );

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "测试标题", "描述", 1L);
        });
    }

    @Test
    @DisplayName("上传视频 - 失败：标题为空")
    void uploadVideo_EmptyTitle() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, "", "描述", 1L);
        });
    }

    @Test
    @DisplayName("上传视频 - 失败：标题过长")
    void uploadVideo_TitleTooLong() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        String longTitle = "a".repeat(101);

        assertThrows(BusinessException.class, () -> {
            videoService.uploadVideo(file, longTitle, "描述", 1L);
        });
    }

    @Test
    @DisplayName("获取视频列表 - 成功")
    void getVideos_Success() {
        List<Video> videoList = Arrays.asList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList, PageRequest.of(0, 10), 1);

        when(videoRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(videoPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));
        when(likeRepository.findByUserIdAndVideoIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        when(favoriteRepository.findByUserIdAndVideoIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());

        Page<VideoDTO> result = videoService.getVideos(0, 10, 1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(videoRepository, times(1)).findAllByOrderByCreatedAtDesc(any(PageRequest.class));
    }

    @Test
    @DisplayName("获取视频列表 - 不带用户ID")
    void getVideos_WithoutUserId() {
        List<Video> videoList = Arrays.asList(testVideo);
        Page<Video> videoPage = new PageImpl<>(videoList, PageRequest.of(0, 10), 1);

        when(videoRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(videoPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<VideoDTO> result = videoService.getVideos(0, 10, null);

        assertNotNull(result);
        verify(likeRepository, never()).findByUserIdAndVideoIdIn(anyLong(), anyList());
        verify(favoriteRepository, never()).findByUserIdAndVideoIdIn(anyLong(), anyList());
    }

    @Test
    @DisplayName("点赞 - 首次点赞成功")
    void toggleLike_FirstTime() {
        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.save(any(VideoLike.class))).thenReturn(new VideoLike());
        doNothing().when(videoRepository).updateLikeCount(1L, 1);

        boolean result = videoService.toggleLike(1L, 1L);

        assertTrue(result);
        verify(likeRepository, times(1)).save(any(VideoLike.class));
        verify(videoRepository, times(1)).updateLikeCount(1L, 1);
    }

    @Test
    @DisplayName("点赞 - 取消点赞成功")
    void toggleLike_Cancel() {
        VideoLike existingLike = new VideoLike();
        existingLike.setId(1L);
        existingLike.setUserId(1L);
        existingLike.setVideoId(1L);

        when(likeRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(existingLike));
        doNothing().when(likeRepository).delete(existingLike);
        doNothing().when(videoRepository).updateLikeCount(1L, -1);

        boolean result = videoService.toggleLike(1L, 1L);

        assertFalse(result);
        verify(likeRepository, times(1)).delete(existingLike);
        verify(videoRepository, times(1)).updateLikeCount(1L, -1);
    }

    @Test
    @DisplayName("收藏 - 首次收藏成功")
    void toggleFavorite_FirstTime() {
        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.empty());
        when(favoriteRepository.save(any(VideoFavorite.class))).thenReturn(new VideoFavorite());
        doNothing().when(videoRepository).updateFavoriteCount(1L, 1);

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertTrue(result);
        verify(favoriteRepository, times(1)).save(any(VideoFavorite.class));
        verify(videoRepository, times(1)).updateFavoriteCount(1L, 1);
    }

    @Test
    @DisplayName("收藏 - 取消收藏成功")
    void toggleFavorite_Cancel() {
        VideoFavorite existingFavorite = new VideoFavorite();
        existingFavorite.setId(1L);
        existingFavorite.setUserId(1L);
        existingFavorite.setVideoId(1L);

        when(favoriteRepository.findByUserIdAndVideoId(1L, 1L)).thenReturn(Optional.of(existingFavorite));
        doNothing().when(favoriteRepository).delete(existingFavorite);
        doNothing().when(videoRepository).updateFavoriteCount(1L, -1);

        boolean result = videoService.toggleFavorite(1L, 1L);

        assertFalse(result);
        verify(favoriteRepository, times(1)).delete(existingFavorite);
        verify(videoRepository, times(1)).updateFavoriteCount(1L, -1);
    }

    @Test
    @DisplayName("添加评论 - 成功")
    void addComment_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        doNothing().when(videoRepository).updateCommentCount(1L, 1);

        Comment result = videoService.addComment(1L, 1L, "测试评论");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(videoRepository, times(1)).updateCommentCount(1L, 1);
    }

    @Test
    @DisplayName("获取评论列表 - 成功")
    void getComments_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");

        List<Comment> commentList = Arrays.asList(comment);
        Page<Comment> commentPage = new PageImpl<>(commentList, PageRequest.of(0, 10), 1);

        when(commentRepository.findByVideoIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class))).thenReturn(commentPage);
        when(userRepository.findAllById(anySet())).thenReturn(Collections.singletonList(testUser));

        Page<Comment> result = videoService.getComments(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(commentRepository, times(1)).findByVideoIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class));
    }

    @Test
    @DisplayName("增加播放量 - 成功")
    void incrementViewCount_Success() {
        doNothing().when(videoRepository).incrementViewCount(1L);

        videoService.incrementViewCount(1L);

        verify(videoRepository, times(1)).incrementViewCount(1L);
    }

    @Test
    @DisplayName("获取所有视频 - 管理员列表")
    void getAllVideos_Success() {
        List<Video> videoList = Arrays.asList(testVideo);
        when(videoRepository.findAll()).thenReturn(videoList);

        List<Video> result = videoService.getAllVideos();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(videoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("删除视频 - 成功")
    void deleteVideo_Success() {
        doNothing().when(videoRepository).deleteById(1L);

        videoService.deleteVideo(1L);

        verify(videoRepository, times(1)).deleteById(1L);
    }
}
