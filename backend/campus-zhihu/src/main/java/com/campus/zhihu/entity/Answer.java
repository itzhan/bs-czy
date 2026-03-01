package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("answer")
public class Answer {
    @TableId(type = IdType.AUTO) private Long id;
    private String content;
    private Long questionId;
    private Long userId;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isAccepted;
    private Integer status; // 0-待审核, 1-已发布, 2-已拒绝
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
    @TableLogic private Integer deleted;
}
