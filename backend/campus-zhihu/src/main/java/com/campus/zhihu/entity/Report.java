package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("report")
public class Report {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private Integer reason;
    private String description;
    private Integer status;
    private String handleNote;
    private Long handleUserId;
    private LocalDateTime handleTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
