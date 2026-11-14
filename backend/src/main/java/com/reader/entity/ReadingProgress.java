package com.reader.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReadingProgress {
    private Long id;
    private Long userId;
    private Integer targetType;   // 0=文章, 1=书籍
    private Long targetId;
    private Long chapterId;
    private BigDecimal scrollPosition;
    private LocalDateTime lastReadAt;
    private LocalDateTime updatedAt;
}
