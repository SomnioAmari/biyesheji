package com.reader.controller;

import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.dto.ProgressRequest;
import com.reader.entity.ReadingProgress;
import com.reader.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService service;

    @GetMapping
    public Result<ReadingProgress> get(@RequestParam int targetType, @RequestParam Long targetId) {
        return Result.success(service.get(UserContext.require(), targetType, targetId));
    }

    @PutMapping
    public Result<Void> report(@Valid @RequestBody ProgressRequest request) {
        service.report(UserContext.require(), request);
        return Result.success();
    }
}
