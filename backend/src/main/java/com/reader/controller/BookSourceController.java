package com.reader.controller;

import com.reader.common.BizException;
import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.dto.BookSourceRequest;
import com.reader.entity.BookSource;
import com.reader.service.BookSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book-sources")
@RequiredArgsConstructor
public class BookSourceController {

    private final BookSourceService service;

    @GetMapping
    public Result<List<BookSource>> list() {
        return Result.success(service.list(UserContext.require()));
    }

    @PostMapping
    public Result<BookSource> create(@Valid @RequestBody BookSourceRequest request) {
        return Result.success(service.create(UserContext.require(), request));
    }

    @PutMapping("/{id}")
    public Result<BookSource> update(@PathVariable Long id, @Valid @RequestBody BookSourceRequest request) {
        return Result.success(service.update(UserContext.require(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.require(), id);
        return Result.success();
    }

    /**
     * URL 导入：抓取 URL 指向的书源 JSON（单个或数组），自动识别 legado 格式。
     */
    @PostMapping("/import-url")
    public Result<List<BookSource>> importUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            throw new BizException("URL 不能为空");
        }
        return Result.success(service.importFromUrl(UserContext.require(), url));
    }

    /**
     * 粘贴导入：直接粘贴书源 JSON（legado 或本系统格式，单个或数组）。
     */
    @PostMapping("/import")
    public Result<List<BookSource>> importJson(@RequestBody Map<String, String> body) {
        String ruleJson = body.get("ruleJson");
        if (ruleJson == null || ruleJson.isBlank()) {
            throw new BizException("书源 JSON 不能为空");
        }
        return Result.success(service.importJson(UserContext.require(), ruleJson));
    }

    /**
     * 原地重转换已导入书源的规则（修复升级转换器后遗留的旧数据）。
     */
    @PostMapping("/reconvert")
    public Result<Integer> reconvert() {
        return Result.success(service.reconvertAll(UserContext.require()));
    }
}
