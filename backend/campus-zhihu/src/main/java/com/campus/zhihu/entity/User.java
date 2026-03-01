package com.campus.zhihu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String bio;
    private Integer role;       // 0-学生, 1-教师, 2-管理员
    private Integer status;     // 0-禁用, 1-正常
    private Integer gender;     // 0-未知, 1-男, 2-女
    private String department;
    private Integer questionCount;
    private Integer answerCount;
    private Integer followerCount;
    private Integer followingCount;
    private Integer likeCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
