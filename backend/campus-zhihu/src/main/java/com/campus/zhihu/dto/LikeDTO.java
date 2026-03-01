package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeDTO {
    @NotNull private Integer targetType; // 1-问题, 2-回答, 3-评论
    @NotNull private Long targetId;
}
