package com.shortvideo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.dto.ActionRequest;
import com.shortvideo.dto.CommentRequest;
import com.shortvideo.dto.VideoDTO;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.Video;
import com.shortvideo.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private VideoService videoService;

    @InjectMocks
    private VideoController videoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(videoController).build();
    }

    @Test
    void listVideos_Success() throws Exception {
        VideoDTO dto = new VideoDTO();
        dto.setId(1L);
        dto.setTitle("测试视频");
        Page<VideoDTO> page = new PageImpl<>(Collections.singletonList(dto));
        when(videoService.getVideos(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].title").value("测试视频"));
    }

    @Test
    void listVideos_WithUserId() throws Exception {
        VideoDTO dto = new VideoDTO();
        dto.setId(1L);
        dto.setTitle("测试视频");
        dto.setLiked(true);
        dto.setFavorited(false);
        Page<VideoDTO> page = new PageImpl<>(Collections.singletonList(dto));
        when(videoService.getVideos(0, 10, 1L)).thenReturn(page);

        mockMvc.perform(get("/api/video/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].liked").value(true));
    }

    @Test
    void likeVideo_Success() throws Exception {
        when(videoService.toggleLike(1L, 1L)).thenReturn(true);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void unlikeVideo_Success() throws Exception {
        when(videoService.toggleLike(1L, 1L)).thenReturn(false);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    void favoriteVideo_Success() throws Exception {
        when(videoService.toggleFavorite(1L, 1L)).thenReturn(true);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void unfavoriteVideo_Success() throws Exception {
        when(videoService.toggleFavorite(1L, 1L)).thenReturn(false);

        ActionRequest request = new ActionRequest();
        request.setVideoId(1L);
        request.setUserId(1L);

        mockMvc.perform(post("/api/video/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("测试评论"));
    }

    @Test
    void getComments_Success() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setVideoId(1L);
        comment.setContent("测试评论");
        Page<Comment> page = new PageImpl<>(Collections.singletonList(comment));
        when(videoService.getComments(eq(1L), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/video/comments/1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void viewVideo_Success() throws Exception {
        mockMvc.perform(post("/api/video/view/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void adminList_Success() throws Exception {
        Video video = new Video();
        video.setId(1L);
        video.setTitle("测试视频");
        when(videoService.getAllVideos()).thenReturn(Collections.singletonList(video));

        mockMvc.perform(get("/api/video/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("测试视频"));
    }

    @Test
    void deleteVideo_Success() throws Exception {
        mockMvc.perform(delete("/api/video/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
