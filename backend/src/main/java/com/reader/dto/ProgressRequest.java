package com.reader.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProgressRequest {
    @NotNull(message = "目标类型不能为空")
    private Integer targetType;   // 0=文章, 1=书籍

    @NotNull(message = "目标 ID 不能为空")
    private Long targetId;

    private Long chapterId;
    private BigDecimal scrollPosition;
}
