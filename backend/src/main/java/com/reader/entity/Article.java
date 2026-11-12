package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {
    private Long id;
    private Long sourceId;
    private String title;
    private String author;
    private String sourceName;
    private String url;
    private String urlHash;
    private String summary;
    private String content;
    private String cover;
    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;
    private LocalDateTime createdAt;
}
