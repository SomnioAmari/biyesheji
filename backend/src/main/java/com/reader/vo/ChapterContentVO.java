package com.reader.vo;

public record ChapterContentVO(Long chapterId, String name, String content, Long prevId, Long nextId) {
}
