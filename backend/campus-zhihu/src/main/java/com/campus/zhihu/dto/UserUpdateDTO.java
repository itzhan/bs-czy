package com.campus.zhihu.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String bio;
    private Integer gender;
    private String department;
}
