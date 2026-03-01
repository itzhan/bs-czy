package com.campus.zhihu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportDTO {
    @NotNull private Integer targetType;
    @NotNull private Long targetId;
    @NotNull private Integer reason;
    private String description;
}
