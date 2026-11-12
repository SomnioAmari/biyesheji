package com.reader.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArticleDetailVO(Long id,
                              String title,
                              String author,
                              String sourceName,
                              String cover,
                              LocalDateTime publishedAt,
                              String url,
                              String content,
                              boolean favorited,
                              BigDecimal scrollPosition) {
}
