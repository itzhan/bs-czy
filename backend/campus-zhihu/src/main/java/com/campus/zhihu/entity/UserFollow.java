package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long followUserId;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
}
