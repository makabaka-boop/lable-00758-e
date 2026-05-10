package com.shortvideo.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ActionRequest {
    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
