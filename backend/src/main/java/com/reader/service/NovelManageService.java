package com.reader.service;

import com.reader.common.BizException;
import com.reader.common.PageResult;
import com.reader.dto.ReviewRequest;
import com.reader.entity.NovelBook;
import com.reader.entity.NovelChapter;
import com.reader.entity.User;
import com.reader.mapper.NovelBookMapper;
import com.reader.mapper.NovelChapterMapper;
import com.reader.mapper.UserMapper;
import com.reader.parse.ChapterSplitter;
import com.reader.vo.AdminBookVO;
import com.reader.vo.LibraryBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NovelManageService {

    private final NovelBookMapper bookMapper;
    private final NovelChapterMapper chapterMapper;
    private final UserMapper userMapper;
    private final FileStorageService fileStorageService;

    /**
     * 用户上传书籍：TXT 文件或粘贴全文，自动拆分章节，初始为待审核状态。
     */
    @Transactional
    public LibraryBookVO upload(Long userId, String title, String author, String category,
                                String intro, MultipartFile cover, MultipartFile file, String content) {
        if (title == null || title.isBlank()) {
            throw new BizException("书名不能为空");
        }
        String text = resolveText(file, content);
        if (text == null || text.isBlank()) {
            throw new BizException("请上传 TXT 文件或粘贴正文内容");
        }
        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);
        if (chapters.isEmpty()) {
            throw new BizException("未能从内容中解析出章节");
        }

        NovelBook book = new NovelBook();
        book.setSourceId(null);
        book.setUploaderId(userId);
        book.setName(title.trim());
        book.setAuthor(author == null || author.isBlank() ? "佚名" : author.trim());
        book.setCategory(category == null || category.isBlank() ? null : category.trim());
        book.setIntro(intro == null ? null : intro.trim());
        book.setCover(fileStorageService.saveCover(cover));
        book.setStatus("连载");
        book.setReviewStatus(0);
        bookMapper.insert(book);

        List<NovelChapter> entities = new ArrayList<>();
        int index = 0;
        for (ChapterSplitter.Chapter c : chapters) {
            NovelChapter ch = new NovelChapter();
            ch.setBookId(book.getId());
            ch.setName(c.name());
            ch.setContent(c.content());
            ch.setChapterIndex(index++);
            entities.add(ch);
        }
        int batchSize = 500;
        for (int i = 0; i < entities.size(); i += batchSize) {
            chapterMapper.insertBatch(new ArrayList<>(entities.subList(i, Math.min(i + batchSize, entities.size()))));
        }
        return toLibraryVO(book, entities.size());
    }

    public List<LibraryBookVO> listMine(Long userId) {
        return bookMapper.findByUploader(userId).stream()
                .map(b -> toLibraryVO(b, chapterMapper.countByBook(b.getId())))
                .toList();
    }

    public PageResult<AdminBookVO> listForReview(Integer reviewStatus, String keyword, int page, int size) {
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();
        int offset = (page - 1) * size;
        List<NovelBook> books = bookMapper.findForReview(reviewStatus, kw, offset, size);
        long total = bookMapper.countForReview(reviewStatus, kw);

        Map<Long, String> uploaderNames = new HashMap<>();
        List<AdminBookVO> vos = new ArrayList<>();
        for (NovelBook b : books) {
            String uploader = null;
            if (b.getUploaderId() != null) {
                uploader = uploaderNames.computeIfAbsent(b.getUploaderId(), id -> {
                    User u = userMapper.findById(id);
                    return u == null ? null : (u.getNickname() != null ? u.getNickname() : u.getUsername());
                });
            }
            vos.add(new AdminBookVO(b.getId(), b.getName(), b.getAuthor(), b.getCover(), b.getCategory(),
                    b.getReviewStatus(), b.getReviewReason(), uploader, chapterMapper.countByBook(b.getId()),
                    b.getCreatedAt()));
        }
        return PageResult.of(total, vos);
    }

    @Transactional
    public void review(Long adminId, Long bookId, ReviewRequest req) {
        NovelBook book = requireUploadedBook(bookId);
        int status = req.isApproved() ? 1 : 2;
        bookMapper.updateReview(bookId, status,
                req.isApproved() ? null : (req.getReason() == null ? "" : req.getReason().trim()),
                adminId);
    }

    @Transactional
    public void deleteBook(Long operatorId, Long bookId, boolean admin) {
        NovelBook book = requireUploadedBook(bookId);
        if (!admin && !operatorId.equals(book.getUploaderId())) {
            throw new BizException(403, "无权删除该作品");
        }
        chapterMapper.deleteByBook(bookId);
        bookMapper.delete(bookId);
    }

    private NovelBook requireUploadedBook(Long bookId) {
        NovelBook book = bookMapper.findById(bookId);
        if (book == null || book.getSourceId() != null) {
            throw new BizException(404, "上传作品不存在");
        }
        return book;
    }

    private String resolveText(MultipartFile file, String content) {
        if (file != null && !file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                // UTF-16 BOM
                if (bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
                    return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
                }
                if (bytes.length >= 2 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
                    return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
                }
                String utf8 = new String(bytes, StandardCharsets.UTF_8);
                if (utf8.indexOf('\uFFFD') >= 0) {
                    return new String(bytes, Charset.forName("GBK"));
                }
                return utf8;
            } catch (Exception e) {
                throw new BizException("文件读取失败：" + e.getMessage());
            }
        }
        return content;
    }

    private LibraryBookVO toLibraryVO(NovelBook b, int chapterCount) {
        return new LibraryBookVO(b.getId(), b.getName(), b.getAuthor(), b.getCover(), b.getIntro(),
                b.getCategory(), b.getReviewStatus(), b.getReviewReason(), chapterCount, b.getCreatedAt());
    }
}
