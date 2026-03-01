package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("question_draft")
public class QuestionDraft {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String title;
    private String content;
    private String tagIds;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
