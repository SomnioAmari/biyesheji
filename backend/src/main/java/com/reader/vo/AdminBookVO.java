package com.reader.vo;

import java.time.LocalDateTime;

public record AdminBookVO(Long bookId,
                          String name,
                          String author,
                          String cover,
                          String category,
                          Integer reviewStatus,
                          String reviewReason,
                          String uploaderName,
                          Integer chapterCount,
                          LocalDateTime createdAt) {
}
