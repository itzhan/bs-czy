package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("user_like")
public class UserLike {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Integer targetType; // 1-问题, 2-回答, 3-评论
    private Long targetId;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
