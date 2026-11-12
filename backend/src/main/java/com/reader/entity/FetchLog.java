package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FetchLog {
    private Long id;
    private Integer sourceType;   // 0=新闻源, 1=书源
    private Long sourceId;
    private Integer status;       // 1=成功, 0=失败
    private String message;
    private Integer fetchedCount;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
