package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NovelBook {
    private Long id;
    private Long sourceId;
    private String name;
    private String author;
    private String cover;
    private String intro;
    private String url;
    private String tocUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
