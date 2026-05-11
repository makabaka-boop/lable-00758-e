package com.shortvideo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.dto.ActionRequest;
import com.shortvideo.dto.CommentRequest;
import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.Video;
import com.shortvideo.service.VideoService;
import com.shortvideo.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("VideoController 接口测试")
class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VideoService videoService;

    @Autowired
    private JwtUtil jwtUtil;

    private String getAuthToken() {
        return "Bearer " + jwtUtil.generateToken(1L, "testuser", false);
    }

    @Test
    @DisplayName("获取视频列表成功 - 无需认证")
    void listVideos_Success() throws Exception {
        Video video = new Video();
        video.setId(1L);
        video.setTitle("测试视频");
        video.setDescription("测试描述");
        video.setVideoUrl("/uploads/videos/test.mp4");
        video.setCreatedAt(LocalDateTime.now());

        VideoDTO videoDTO = VideoDTO.fromEntity(video);
        Page<VideoDTO> videoPage = new PageImpl<>(Collections.singletonList(videoDTO), PageRequest.of(0, 10), 1);

        when(videoService.getVideos(0, 10, null)).thenReturn(videoPage);

        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("测试视频"));
    }

    @Test
    @DisplayName("获取视频列表 - 参数校验失败 - page为负数")
    void listVideos_InvalidPage() throws Exception {
        mockMvc.perform(get("/api/video/list")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("获取视频列表 - 参数校验失败 - size为0")
    void listVideos_InvalidSize() throws Exception {
        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("点赞视频成功 - 需要认证")
    void likeVideo_Success() throws Exception {
        when(videoService.toggleLike(1L, 1L)).thenReturn(true);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/like")
                        .header("Authorization", getAuthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("点赞视频失败 - 未认证")
    void likeVideo_Unauthorized() throws Exception {
        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("收藏视频成功 - 需要认证")
    void favoriteVideo_Success() throws Exception {
        when(videoService.toggleFavorite(1L, 1L)).thenReturn(false);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/favorite")
                        .header("Authorization", getAuthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("添加评论成功 - 需要认证")
    void addComment_Success() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");

        when(videoService.addComment(1L, 1L, "测试评论")).thenReturn(comment);

        CommentRequest request = new CommentRequest();
        request.setVideoId(1L);
        request.setUserId(1L);
        request.setContent("测试评论");

        mockMvc.perform(post("/api/video/comment")
                        .header("Authorization", getAuthToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("测试评论"));
    }

    @Test
    @DisplayName("获取评论列表成功 - 无需认证")
    void getComments_Success() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setUserId(1L);
        comment.setContent("测试评论");
        comment.setCreatedAt(LocalDateTime.now());

        Page<Comment> commentPage = new PageImpl<>(Collections.singletonList(comment), PageRequest.of(0, 20), 1);

        when(videoService.getComments(1L, 0, 20)).thenReturn(commentPage);

        mockMvc.perform(get("/api/video/comments/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("增加观看次数成功 - 无需认证")
    void viewVideo_Success() throws Exception {
        mockMvc.perform(post("/api/video/view/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传视频成功 - 需要认证")
    void uploadVideo_Success() throws Exception {
        Video video = new Video();
        video.setId(1L);
        video.setTitle("测试视频");
        video.setVideoUrl("/uploads/videos/test.mp4");

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.mp4", "video/mp4", "test content".getBytes()
        );

        when(videoService.uploadVideo(any(), eq("测试视频"), eq("测试描述"), eq(1L))).thenReturn(video);

        mockMvc.perform(multipart("/api/video/upload")
                        .file(file)
                        .param("title", "测试视频")
                        .param("description", "测试描述")
                        .param("userId", "1")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试视频"));
    }

    @Test
    @DisplayName("获取管理员视频列表成功 - 需要认证")
    void getAdminVideos_Success() throws Exception {
        Video video = new Video();
        video.setId(1L);
        video.setTitle("测试视频");

        when(videoService.getAllVideos()).thenReturn(Collections.singletonList(video));

        mockMvc.perform(get("/api/video/admin/list")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("测试视频"));
    }

    @Test
    @DisplayName("删除视频成功 - 需要认证")
    void deleteVideo_Success() throws Exception {
        mockMvc.perform(delete("/api/video/admin/1")
                        .header("Authorization", getAuthToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
