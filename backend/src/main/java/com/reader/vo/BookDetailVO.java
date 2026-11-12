package com.reader.vo;

public record BookDetailVO(Long bookId,
                           String name,
                           String author,
                           String cover,
                           String intro,
                           String status,
                           boolean inShelf,
                           Integer chapterCount) {
}
