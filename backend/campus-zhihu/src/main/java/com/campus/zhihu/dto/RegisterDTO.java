package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50字符")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50字符")
    private String password;
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    private String email;
    private Integer role; // 0-学生, 1-教师
    private String department;
}
