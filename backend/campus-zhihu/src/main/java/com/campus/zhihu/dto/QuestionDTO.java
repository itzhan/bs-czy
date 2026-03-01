package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private List<Long> tagIds;
    private Integer status; // 0-草稿, 1-发布
}
