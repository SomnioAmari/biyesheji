package com.reader.controller;

import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.service.NovelService;
import com.reader.vo.BookshelfVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final NovelService service;

    @GetMapping
    public Result<List<BookshelfVO>> list() {
        return Result.success(service.listShelf(UserContext.require()));
    }

    @PostMapping("/{bookId}")
    public Result<Void> add(@PathVariable Long bookId) {
        service.addToShelf(UserContext.require(), bookId);
        return Result.success();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        service.removeFromShelf(UserContext.require(), bookId);
        return Result.success();
    }
}
