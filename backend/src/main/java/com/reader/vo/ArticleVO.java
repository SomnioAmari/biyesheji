package com.reader.vo;

import java.time.LocalDateTime;

public record ArticleVO(Long id,
                        String title,
                        String author,
                        String sourceName,
                        String summary,
                        String cover,
                        LocalDateTime publishedAt,
                        String url,
                        boolean favorited) {
}
