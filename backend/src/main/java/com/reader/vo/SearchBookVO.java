package com.reader.vo;

public record SearchBookVO(Long sourceId,
                           String sourceName,
                           String name,
                           String author,
                           String cover,
                           String intro,
                           String bookUrl,
                           Long bookId,
                           boolean inShelf) {
}
