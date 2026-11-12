package com.reader.vo;

public record BookshelfVO(Long bookId,
                          String name,
                          String author,
                          String cover,
                          Integer chapterCount,
                          Long lastChapterId,
                          String lastChapterName) {
}
