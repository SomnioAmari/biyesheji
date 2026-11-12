package com.reader.mapper;

import com.reader.entity.NovelChapter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NovelChapterMapper {

    @Select("SELECT id, book_id, name, url, chapter_index, created_at FROM novel_chapter WHERE book_id = #{bookId} ORDER BY chapter_index ASC")
    List<NovelChapter> findByBook(Long bookId);

    @Select("SELECT * FROM novel_chapter WHERE book_id = #{bookId} AND chapter_index = #{index}")
    NovelChapter findByBookAndIndex(@Param("bookId") Long bookId, @Param("index") int index);

    @Select("SELECT * FROM novel_chapter WHERE id = #{id}")
    NovelChapter findById(Long id);

    @Insert("INSERT INTO novel_chapter(book_id, name, url, chapter_index) VALUES(#{bookId}, #{name}, #{url}, #{chapterIndex})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NovelChapter chapter);

    @Insert("<script>INSERT INTO novel_chapter(book_id, name, url, chapter_index) VALUES " +
            "<foreach collection='list' item='c' separator=','>" +
            "(#{c.bookId}, #{c.name}, #{c.url}, #{c.chapterIndex})" +
            "</foreach></script>")
    int insertBatch(@Param("list") List<NovelChapter> chapters);

    @Update("UPDATE novel_chapter SET content = #{content} WHERE id = #{id}")
    int updateContent(@Param("id") Long id, @Param("content") String content);

    @Select("SELECT COUNT(*) FROM novel_chapter WHERE book_id = #{bookId}")
    int countByBook(Long bookId);
}
