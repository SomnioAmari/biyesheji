package com.reader.controller;

import com.reader.common.Result;
import com.reader.common.UserContext;
import com.reader.service.NovelManageService;
import com.reader.service.NovelService;
import com.reader.vo.BookDetailVO;
import com.reader.vo.ChapterContentVO;
import com.reader.vo.ChapterVO;
import com.reader.vo.LibraryBookVO;
import com.reader.vo.SearchBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/novel")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService service;
    private final NovelManageService manageService;

    /** 内部书库搜索（已审核通过的上传书籍） */
    @GetMapping("/search")
    public Result<List<LibraryBookVO>> searchLibrary(@RequestParam String keyword) {
        return Result.success(service.searchLibrary(keyword.trim()));
    }

    /** 自定义书源搜索（内部书库搜不到时的兜底） */
    @GetMapping("/search-sources")
    public Result<List<SearchBookVO>> searchSources(@RequestParam String keyword) {
        return Result.success(service.searchSources(UserContext.require(), keyword.trim()));
    }

    /** 用户上传书籍（TXT 文件或粘贴全文），封面图片可选 */
    @PostMapping("/upload")
    public Result<LibraryBookVO> upload(@RequestParam String title,
                                        @RequestParam(required = false) String author,
                                        @RequestParam(required = false) String category,
                                        @RequestParam(required = false) String intro,
                                        @RequestPart(required = false) MultipartFile cover,
                                        @RequestPart(required = false) MultipartFile file,
                                        @RequestParam(required = false) String content) {
        return Result.success(manageService.upload(UserContext.require(),
                title, author, category, intro, cover, file, content));
    }

    /** 我的上传作品列表 */
    @GetMapping("/mine")
    public Result<List<LibraryBookVO>> mine() {
        return Result.success(manageService.listMine(UserContext.require()));
    }

    /** 删除我的上传作品 */
    @DeleteMapping("/mine/{bookId}")
    public Result<Void> deleteMine(@PathVariable Long bookId) {
        manageService.deleteBook(UserContext.require(), bookId, false);
        return Result.success();
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
