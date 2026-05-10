package com.shortvideo.controller;

import com.shortvideo.dto.ActionRequest;
import com.shortvideo.dto.CommentRequest;
import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.Video;
import com.shortvideo.interceptor.JwtInterceptor;
import com.shortvideo.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
@DisplayName("VideoController 接口测试")
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VideoService videoService;

    @MockBean
    private JwtInterceptor jwtInterceptor;

    private Video testVideo;
    private VideoDTO testVideoDTO;
    private Comment testComment;

    @BeforeEach
    void setUp() throws Exception {
        testVideo = new Video();
        testVideo.setId(1L);
        testVideo.setTitle("测试视频");
        testVideo.setDescription("测试描述");
        testVideo.setVideoUrl("/uploads/videos/test.mp4");
        testVideo.setUserId(1L);
        testVideo.setLikeCount(10);
        testVideo.setFavoriteCount(5);
        testVideo.setCommentCount(3);
        testVideo.setViewCount(100);

        testVideoDTO = VideoDTO.fromEntity(testVideo);
        testVideoDTO.setUsername("测试用户");
        testVideoDTO.setLiked(false);
        testVideoDTO.setFavorited(false);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setVideoId(1L);
        testComment.setUserId(1L);
        testComment.setContent("测试评论内容");
        testComment.setUsername("测试用户");
        
        when(jwtInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @DisplayName("获取视频列表 - 成功")
    void getVideoList_Success() throws Exception {
        List<VideoDTO> videoList = Arrays.asList(testVideoDTO);
        Page<VideoDTO> videoPage = new PageImpl<>(videoList, PageRequest.of(0, 10), 1);

        when(videoService.getVideos(0, 10, null)).thenReturn(videoPage);

        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("测试视频"))
                .andExpect(jsonPath("$.data.content[0].username").value("测试用户"));
    }

    @Test
    @DisplayName("获取视频列表 - 带用户ID")
    void getVideoList_WithUserId() throws Exception {
        testVideoDTO.setLiked(true);
        List<VideoDTO> videoList = Arrays.asList(testVideoDTO);
        Page<VideoDTO> videoPage = new PageImpl<>(videoList, PageRequest.of(0, 10), 1);

        when(videoService.getVideos(0, 10, 1L)).thenReturn(videoPage);

        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].liked").value(true));
    }

    @Test
    @DisplayName("获取视频列表 - 参数校验：负数页码")
    void getVideoList_Validation_NegativePage() throws Exception {
        mockMvc.perform(get("/api/video/list")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("点赞视频 - 成功点赞")
    void likeVideo_Success() throws Exception {
        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        when(videoService.toggleLike(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("点赞视频 - 取消点赞")
    void likeVideo_Cancel() throws Exception {
        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        when(videoService.toggleLike(1L, 1L)).thenReturn(false);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("点赞视频 - 参数校验：缺少视频ID")
    void likeVideo_Validation_MissingVideoId() throws Exception {
        ActionRequest request = new ActionRequest();
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("收藏视频 - 成功收藏")
    void favoriteVideo_Success() throws Exception {
        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        when(videoService.toggleFavorite(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/video/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("发表评论 - 成功")
    void addComment_Success() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setVideoId(1L);
        request.setUserId(1L);
        request.setContent("测试评论");

        when(videoService.addComment(1L, 1L, "测试评论")).thenReturn(testComment);

        mockMvc.perform(post("/api/video/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("测试评论内容"));
    }

    @Test
    @DisplayName("发表评论 - 参数校验：内容为空")
    void addComment_Validation_EmptyContent() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setVideoId(1L);
        request.setUserId(1L);
        request.setContent("");

        mockMvc.perform(post("/api/video/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("获取评论列表 - 成功")
    void getComments_Success() throws Exception {
        List<Comment> commentList = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(commentList, PageRequest.of(0, 10), 1);

        when(videoService.getComments(1L, 0, 10)).thenReturn(commentPage);

        mockMvc.perform(get("/api/video/comments/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].content").value("测试评论内容"));
    }

    @Test
    @DisplayName("增加播放量 - 成功")
    void incrementViewCount_Success() throws Exception {
        doNothing().when(videoService).incrementViewCount(1L);

        mockMvc.perform(post("/api/video/view/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传视频 - 成功")
    void uploadVideo_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        when(videoService.uploadVideo(any(), eq("测试标题"), eq("测试描述"), eq(1L)))
                .thenReturn(testVideo);

        mockMvc.perform(multipart("/api/video/upload")
                        .file(file)
                        .param("title", "测试标题")
                        .param("description", "测试描述")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试视频"));
    }

    @Test
    @DisplayName("获取管理员视频列表 - 成功")
    void getAdminVideoList_Success() throws Exception {
        List<Video> videoList = Arrays.asList(testVideo);
        when(videoService.getAllVideos()).thenReturn(videoList);

        mockMvc.perform(get("/api/video/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("测试视频"));
    }

    @Test
    @DisplayName("删除视频 - 成功")
    void deleteVideo_Success() throws Exception {
        doNothing().when(videoService).deleteVideo(1L);

        mockMvc.perform(delete("/api/video/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传视频 - 异常处理：服务层抛出异常")
    void uploadVideo_Exception() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.mp4",
                "video/mp4",
                "fake video content".getBytes()
        );

        when(videoService.uploadVideo(any(), eq("测试标题"), any(), eq(1L)))
                .thenThrow(new RuntimeException("上传失败"));

        mockMvc.perform(multipart("/api/video/upload")
                        .file(file)
                        .param("title", "测试标题")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
