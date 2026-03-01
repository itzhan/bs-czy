package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long senderId;
    private Integer type; // 1-回答, 2-评论, 3-点赞, 4-关注, 5-采纳, 6-系统
    private String title;
    private String content;
    private Integer targetType;
    private Long targetId;
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
