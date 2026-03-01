package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private Integer status;  // 0-草稿, 1-已发布, 2-已关闭, 3-待审核, 4-已拒绝
    private Integer viewCount;
    private Integer answerCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer isTop;
    private Long acceptedAnswerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
