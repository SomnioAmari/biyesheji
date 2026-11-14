package com.reader.vo;

import java.time.LocalDateTime;

public record LibraryBookVO(Long bookId,
                            String name,
                            String author,
                            String cover,
                            String intro,
                            String category,
                            Integer reviewStatus,
                            String reviewReason,
                            Integer chapterCount,
                            LocalDateTime createdAt) {
}
