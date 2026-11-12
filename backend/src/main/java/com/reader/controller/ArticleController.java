package com.reader.controller;

import com.reader.common.PageResult;
import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.service.ArticleService;
import com.reader.vo.ArticleDetailVO;
import com.reader.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService service;

    @GetMapping
    public Result<PageResult<ArticleVO>> feed(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) Long sourceId) {
        return Result.success(service.feed(UserContext.require(), sourceId, page, size));
    }

    @GetMapping("/favorites")
    public Result<List<ArticleVO>> favorites() {
        return Result.success(service.listFavorites(UserContext.require()));
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailVO> detail(@PathVariable Long id) {
        return Result.success(service.detail(UserContext.require(), id));
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id) {
        service.favorite(UserContext.require(), id);
        return Result.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id) {
        service.unfavorite(UserContext.require(), id);
        return Result.success();
    }
}
