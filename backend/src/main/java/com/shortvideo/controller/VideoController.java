package com.shortvideo.controller;

import com.shortvideo.annotation.Log;
import com.shortvideo.dto.*;
import com.shortvideo.entity.Comment;
import com.shortvideo.entity.Video;
import com.shortvideo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
@Validated
public class VideoController {
    @Autowired
    private VideoService videoService;

    @Log("上传视频")
    @PostMapping("/upload")
    public Result<Video> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam("title") String title,
                                @RequestParam(value = "description", required = false) String description,
                                @RequestParam("userId") Long userId) {
        try {
            Video video = videoService.uploadVideo(file, title, description, userId);
            return Result.success(video);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<VideoDTO>> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) int size,
                                       @RequestParam(required = false) Long userId) {
        return Result.success(videoService.getVideos(page, Math.min(size, 50), userId));
    }

    @Log("点赞视频")
    @PostMapping("/like")
    public Result<Boolean> like(@Validated @RequestBody ActionRequest request) {
        boolean liked = videoService.toggleLike(request.getVideoId(), request.getUserId());
        return Result.success(liked);
    }

    @Log("收藏视频")
    @PostMapping("/favorite")
    public Result<Boolean> favorite(@Validated @RequestBody ActionRequest request) {
        boolean favorited = videoService.toggleFavorite(request.getVideoId(), request.getUserId());
        return Result.success(favorited);
    }

    @Log("发表评论")
    @PostMapping("/comment")
    public Result<Comment> comment(@Validated @RequestBody CommentRequest request) {
        Comment comment = videoService.addComment(request.getVideoId(), request.getUserId(), request.getContent());
        return Result.success(comment);
    }

    @GetMapping("/comments/{videoId}")
    public Result<Page<Comment>> comments(@PathVariable Long videoId,
                                          @RequestParam(defaultValue = "0") @Min(0) int page,
                                          @RequestParam(defaultValue = "20") @Min(1) int size) {
        return Result.success(videoService.getComments(videoId, page, Math.min(size, 100)));
    }

    @PostMapping("/view/{videoId}")
    public Result<Void> view(@PathVariable Long videoId) {
        videoService.incrementViewCount(videoId);
        return Result.success(null);
    }

    @GetMapping("/admin/list")
    public Result<List<Video>> adminList() {
        return Result.success(videoService.getAllVideos());
    }

    @Log("删除视频")
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return Result.success(null);
    }
}
