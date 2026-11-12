package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Bookshelf {
    private Long id;
    private Long userId;
    private Long bookId;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
