package com.reader.controller;

import com.reader.common.PageResult;
import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.dto.ReviewRequest;
import com.reader.dto.UserPasswordRequest;
import com.reader.dto.UserRoleRequest;
import com.reader.dto.UserStatusRequest;
import com.reader.service.AdminService;
import com.reader.service.NovelManageService;
import com.reader.vo.AdminBookVO;
import com.reader.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NovelManageService novelManageService;

    // ---------- 用户管理 ----------

    @GetMapping("/users")
    public Result<PageResult<UserVO>> users(@RequestParam(defaultValue = "") String keyword,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        UserContext.requireAdmin();
        return Result.success(adminService.listUsers(keyword, page, size));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody UserStatusRequest req) {
        UserContext.requireAdmin();
        adminService.updateStatus(id, req.status());
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody UserRoleRequest req) {
        UserContext.requireAdmin();
        adminService.updateRole(id, req.role());
        return Result.success();
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody UserPasswordRequest req) {
        UserContext.requireAdmin();
        adminService.resetPassword(id, req.password());
        return Result.success();
    }

    // ---------- 书籍审核 ----------

    @GetMapping("/novels")
    public Result<PageResult<AdminBookVO>> novels(@RequestParam(required = false) Integer reviewStatus,
                                                  @RequestParam(defaultValue = "") String keyword,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        UserContext.requireAdmin();
        return Result.success(novelManageService.listForReview(reviewStatus, keyword, page, size));
    }

    @PostMapping("/novels/{bookId}/review")
    public Result<Void> review(@PathVariable Long bookId, @RequestBody ReviewRequest req) {
        novelManageService.review(UserContext.requireAdmin(), bookId, req);
        return Result.success();
    }

    @DeleteMapping("/novels/{bookId}")
    public Result<Void> deleteNovel(@PathVariable Long bookId) {
        novelManageService.deleteBook(UserContext.requireAdmin(), bookId, true);
        return Result.success();
    }
}
