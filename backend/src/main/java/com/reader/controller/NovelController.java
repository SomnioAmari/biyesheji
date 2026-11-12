package com.reader.controller;

import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.service.NovelService;
import com.reader.vo.BookDetailVO;
import com.reader.vo.ChapterContentVO;
import com.reader.vo.ChapterVO;
import com.reader.vo.SearchBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/novel")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService service;

    @GetMapping("/search")
    public Result<List<SearchBookVO>> search(@RequestParam String keyword) {
        return Result.success(service.search(UserContext.require(), keyword.trim()));
    }

    @GetMapping("/detail")
    public Result<BookDetailVO> detail(@RequestParam Long sourceId, @RequestParam String bookUrl) {
        return Result.success(service.detail(UserContext.require(), sourceId, bookUrl));
    }

    @GetMapping("/{bookId}")
    public Result<BookDetailVO> detailById(@PathVariable Long bookId) {
        return Result.success(service.getDetail(UserContext.require(), bookId));
    }

    @GetMapping("/{bookId}/catalog")
    public Result<List<ChapterVO>> catalog(@PathVariable Long bookId) {
        return Result.success(service.catalog(UserContext.require(), bookId));
    }

    @GetMapping("/{bookId}/{chapterId}")
    public Result<ChapterContentVO> chapterContent(@PathVariable Long bookId, @PathVariable Long chapterId) {
        return Result.success(service.chapterContent(UserContext.require(), bookId, chapterId));
    }
}
