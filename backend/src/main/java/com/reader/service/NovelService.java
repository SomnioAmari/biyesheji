package com.reader.service;

import com.reader.common.BizException;
import com.reader.common.UserContext;
import com.reader.entity.BookSource;
import com.reader.entity.Bookshelf;
import com.reader.entity.NovelBook;
import com.reader.entity.NovelChapter;
import com.reader.entity.ReadingProgress;
import com.reader.mapper.BookSourceMapper;
import com.reader.mapper.BookshelfMapper;
import com.reader.mapper.NovelBookMapper;
import com.reader.mapper.NovelChapterMapper;
import com.reader.mapper.ReadingProgressMapper;
import com.reader.rule.ChapterItem;
import com.reader.rule.RuleBookDetail;
import com.reader.rule.RuleEngine;
import com.reader.rule.SearchItem;
import com.reader.vo.BookDetailVO;
import com.reader.vo.BookshelfVO;
import com.reader.vo.ChapterContentVO;
import com.reader.vo.ChapterVO;
import com.reader.vo.LibraryBookVO;
import com.reader.vo.SearchBookVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 书籍板块：内部书库搜索 + 书源规则搜索（无推荐）+ 详情/目录/章节 + 书架。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NovelService {

    private static final ExecutorService SEARCH_POOL = Executors.newFixedThreadPool(8);

    private final BookSourceMapper bookSourceMapper;
    private final NovelBookMapper bookMapper;
    private final NovelChapterMapper chapterMapper;
    private final BookshelfMapper bookshelfMapper;
    private final ReadingProgressMapper progressMapper;
    private final RuleEngine ruleEngine;

    // ---------- 内部书库搜索（已审核通过的上传书籍） ----------

    public List<LibraryBookVO> searchLibrary(String keyword) {
        return bookMapper.searchLibrary(keyword == null ? null : keyword.trim()).stream()
                .map(b -> new LibraryBookVO(b.getId(), b.getName(), b.getAuthor(), b.getCover(), b.getIntro(),
                        b.getCategory(), b.getReviewStatus(), b.getReviewReason(),
                        chapterMapper.countByBook(b.getId()), b.getCreatedAt()))
                .toList();
    }

    // ---------- 自定义书源搜索（无推荐） ----------

    public List<SearchBookVO> searchSources(Long userId, String keyword) {
        List<BookSource> sources = bookSourceMapper.findByUser(userId).stream()
                .filter(s -> s.getEnabled() != null && s.getEnabled() == 1)
                .toList();
        Set<Long> shelfBookIds = bookshelfMapper.findByUser(userId).stream()
                .map(Bookshelf::getBookId).collect(Collectors.toSet());

        List<CompletableFuture<List<SearchBookVO>>> futures = new ArrayList<>();
        for (BookSource source : sources) {
            futures.add(CompletableFuture.supplyAsync(
                    () -> searchSource(source, keyword, shelfBookIds), SEARCH_POOL));
        }
        List<SearchBookVO> results = new ArrayList<>();
        for (CompletableFuture<List<SearchBookVO>> f : futures) {
            try {
                results.addAll(f.get(15, TimeUnit.SECONDS));
            } catch (Exception e) {
                // 单个书源超时或失败，跳过
            }
        }
        return results;
    }

    private List<SearchBookVO> searchSource(BookSource source, String keyword, Set<Long> shelfBookIds) {
        try {
            List<SearchItem> items = ruleEngine.search(source, keyword);
            List<SearchBookVO> vos = new ArrayList<>();
            for (SearchItem item : items) {
                NovelBook existing = bookMapper.findBySourceNameAuthor(source.getId(), item.name(), item.author());
                Long bookId = existing == null ? null : existing.getId();
                vos.add(new SearchBookVO(source.getId(), source.getName(), item.name(),
                        item.author(), item.cover(), null, item.detailUrl(), bookId,
                        bookId != null && shelfBookIds.contains(bookId)));
            }
            return vos;
        } catch (Exception e) {
            log.warn("书源 [{}] 搜索失败: {}", source.getName(), e.getMessage());
            return List.of();
        }
    }

    // ---------- 详情 / 目录 / 章节 ----------

    public BookDetailVO detail(Long userId, Long sourceId, String bookUrl) {
        BookSource source = requireOwnedSource(userId, sourceId);
        RuleBookDetail d = ruleEngine.detail(source, bookUrl);

        NovelBook book = bookMapper.findBySourceNameAuthor(sourceId, d.name(), d.author());
        if (book == null) {
            book = new NovelBook();
            book.setSourceId(sourceId);
            book.setUploaderId(null);
            book.setName(d.name());
            book.setAuthor(d.author());
            book.setCover(d.cover());
            book.setIntro(d.intro());
            book.setUrl(bookUrl);
            book.setTocUrl(d.tocUrl());
            book.setReviewStatus(1);
            bookMapper.insert(book);
        } else {
            book.setCover(d.cover());
            book.setIntro(d.intro());
            book.setTocUrl(d.tocUrl());
            bookMapper.update(book);
        }

        boolean inShelf = bookshelfMapper.findByUserAndBook(userId, book.getId()) != null;
        int chapterCount = chapterMapper.countByBook(book.getId());
        return new BookDetailVO(book.getId(), book.getName(), book.getAuthor(), book.getCover(),
                book.getIntro(), book.getStatus(), inShelf, chapterCount);
    }

    public BookDetailVO getDetail(Long userId, Long bookId) {
        NovelBook book = requireReadableBook(userId, bookId);
        boolean inShelf = bookshelfMapper.findByUserAndBook(userId, bookId) != null;
        int chapterCount = chapterMapper.countByBook(bookId);
        return new BookDetailVO(book.getId(), book.getName(), book.getAuthor(), book.getCover(),
                book.getIntro(), book.getStatus(), inShelf, chapterCount);
    }

    public List<ChapterVO> catalog(Long userId, Long bookId) {
        NovelBook book = requireReadableBook(userId, bookId);

        List<NovelChapter> existing = chapterMapper.findByBook(bookId);
        if (!existing.isEmpty() || book.getSourceId() == null) {
            return existing.stream().map(c -> new ChapterVO(c.getId(), c.getName(), c.getChapterIndex())).toList();
        }

        BookSource source = bookSourceMapper.findById(book.getSourceId());
        List<ChapterItem> items = ruleEngine.catalog(source, book.getTocUrl());
        List<NovelChapter> chapters = new ArrayList<>();
        int index = 0;
        for (ChapterItem item : items) {
            NovelChapter chapter = new NovelChapter();
            chapter.setBookId(bookId);
            chapter.setName(item.name());
            chapter.setUrl(item.url());
            chapter.setChapterIndex(index++);
            chapters.add(chapter);
        }
        int batchSize = 500;
        for (int i = 0; i < chapters.size(); i += batchSize) {
            chapterMapper.insertBatch(new ArrayList<>(chapters.subList(i, Math.min(i + batchSize, chapters.size()))));
        }
        return chapterMapper.findByBook(bookId).stream()
                .map(c -> new ChapterVO(c.getId(), c.getName(), c.getChapterIndex())).toList();
    }

    public ChapterContentVO chapterContent(Long userId, Long bookId, Long chapterId) {
        NovelBook book = requireReadableBook(userId, bookId);

        NovelChapter chapter = chapterMapper.findById(chapterId);
        if (chapter == null || !chapter.getBookId().equals(bookId)) {
            throw new BizException(404, "章节不存在");
        }
        if ((chapter.getContent() == null || chapter.getContent().isBlank()) && book.getSourceId() != null) {
            BookSource source = bookSourceMapper.findById(book.getSourceId());
            String content = ruleEngine.content(source, chapter.getUrl());
            chapterMapper.updateContent(chapterId, content);
            chapter.setContent(content);
        }

        List<NovelChapter> all = chapterMapper.findByBook(bookId);
        Long prev = null;
        Long next = null;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(chapterId)) {
                if (i > 0) {
                    prev = all.get(i - 1).getId();
                }
                if (i < all.size() - 1) {
                    next = all.get(i + 1).getId();
                }
                break;
            }
        }
        return new ChapterContentVO(chapterId, chapter.getName(),
                chapter.getContent() == null ? "" : chapter.getContent(), prev, next);
    }

    // ---------- 书架 ----------

    public void addToShelf(Long userId, Long bookId) {
        requireReadableBook(userId, bookId);
        if (bookshelfMapper.findByUserAndBook(userId, bookId) != null) {
            return;
        }
        Bookshelf shelf = new Bookshelf();
        shelf.setUserId(userId);
        shelf.setBookId(bookId);
        bookshelfMapper.insert(shelf);
    }

    public void removeFromShelf(Long userId, Long bookId) {
        bookshelfMapper.delete(userId, bookId);
    }

    public List<BookshelfVO> listShelf(Long userId) {
        List<Bookshelf> entries = bookshelfMapper.findByUser(userId);
        List<Long> bookIds = entries.stream().map(Bookshelf::getBookId).toList();
        if (bookIds.isEmpty()) {
            return List.of();
        }
        Map<Long, NovelBook> bookMap = bookMapper.findByIds(bookIds).stream()
                .collect(Collectors.toMap(NovelBook::getId, b -> b, (a, b) -> a));
        List<BookshelfVO> vos = new ArrayList<>();
        for (Bookshelf e : entries) {
            NovelBook b = bookMap.get(e.getBookId());
            if (b == null) {
                continue;
            }
            int chapterCount = chapterMapper.countByBook(b.getId());
            ReadingProgress p = progressMapper.findByUserAndTarget(userId, 1, b.getId());
            Long lastChapterId = p == null ? null : p.getChapterId();
            String lastChapterName = null;
            if (lastChapterId != null) {
                NovelChapter c = chapterMapper.findById(lastChapterId);
                if (c != null) {
                    lastChapterName = c.getName();
                }
            }
            vos.add(new BookshelfVO(b.getId(), b.getName(), b.getAuthor(), b.getCover(),
                    chapterCount, lastChapterId, lastChapterName));
        }
        return vos;
    }

    // ---------- 权限 ----------

    private NovelBook requireBook(Long bookId) {
        NovelBook book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BizException(404, "书籍不存在");
        }
        return book;
    }

    /**
     * 内部上传书籍：审核通过所有人可见；待审核/驳回仅上传者与管理员可见。
     * 书源书籍：仅所属书源拥有者可读。
     */
    private NovelBook requireReadableBook(Long userId, Long bookId) {
        NovelBook book = requireBook(bookId);
        if (book.getSourceId() == null) {
            if (book.getReviewStatus() != null && book.getReviewStatus() == 1) {
                return book;
            }
            if (userId.equals(book.getUploaderId())) {
                return book;
            }
            if (UserContext.role() != null && UserContext.role() == 1) {
                return book;
            }
            throw new BizException(403, "该作品尚未通过审核");
        }
        requireOwnedSource(userId, book.getSourceId());
        return book;
    }

    private BookSource requireOwnedSource(Long userId, Long sourceId) {
        BookSource source = bookSourceMapper.findById(sourceId);
        if (source == null || !source.getUserId().equals(userId)) {
            throw new BizException(403, "无权操作该书源");
        }
        return source;
    }
}
