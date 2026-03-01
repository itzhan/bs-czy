package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnswerDTO {
    @NotBlank(message = "回答内容不能为空")
    private String content;
}
