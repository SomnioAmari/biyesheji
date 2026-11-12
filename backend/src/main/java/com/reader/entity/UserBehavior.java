package com.reader.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBehavior {
    private Long id;
    private Long userId;
    private Integer targetType;
    private Long targetId;
    private Integer clickCount;
    private Integer readDuration;
    private Integer readComplete;
    private LocalDateTime lastActionAt;
    private LocalDateTime updatedAt;
}
