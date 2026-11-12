package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookSource {
    private Long id;
    private Long userId;
    private String name;
    private String url;
    private String ruleJson;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
