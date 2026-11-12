package com.reader.controller;

import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.dto.NewsSourceRequest;
import com.reader.entity.FetchLog;
import com.reader.entity.NewsSource;
import com.reader.service.NewsSourceService;
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

@RestController
@RequestMapping("/api/news-sources")
@RequiredArgsConstructor
public class NewsSourceController {

    private final NewsSourceService service;

    @GetMapping
    public Result<List<NewsSource>> list() {
        return Result.success(service.list(UserContext.require()));
    }

    @PostMapping
    public Result<NewsSource> create(@Valid @RequestBody NewsSourceRequest request) {
        return Result.success(service.create(UserContext.require(), request));
    }

    @PutMapping("/{id}")
    public Result<NewsSource> update(@PathVariable Long id, @Valid @RequestBody NewsSourceRequest request) {
        return Result.success(service.update(UserContext.require(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(UserContext.require(), id);
        return Result.success();
    }

    @PostMapping("/{id}/fetch")
    public Result<FetchLog> fetch(@PathVariable Long id) {
        return Result.success(service.fetchNow(UserContext.require(), id));
    }
}
