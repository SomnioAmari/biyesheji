package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NovelChapter {
    private Long id;
    private Long bookId;
    private String name;
    private String url;
    private String content;
    private Integer chapterIndex;
    private LocalDateTime createdAt;
}
