package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {
    @NotBlank(message = "评论内容不能为空")
    private String content;
    @NotNull(message = "目标类型不能为空")
    private Integer targetType; // 1-问题, 2-回答
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
    private Long parentId;
    private Long replyUserId;
}
