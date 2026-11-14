package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NovelBook {
    private Long id;
    private Long sourceId;        // 书源书籍归属书源；上传书籍为 null
    private Long uploaderId;      // 上传者（上传书籍）
    private String name;
    private String author;
    private String cover;
    private String intro;
    private String category;
    private String url;
    private String tocUrl;
    private String status;
    private Integer reviewStatus; // 0=待审核, 1=已通过, 2=已拒绝
    private String reviewReason;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
