package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsSource {
    private Long id;
    private Long userId;
    private Integer type;        // 0=RSS, 1=网页源
    private String name;
    private String url;
    private String category;
    private String ruleJson;
    private Integer weight;
    private Integer fetchInterval;
    private Integer enabled;
    private LocalDateTime lastFetchAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
