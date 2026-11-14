package com.reader.mapper;

import com.reader.entity.NovelBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface NovelBookMapper {

    @Select("SELECT * FROM novel_book WHERE id = #{id}")
    NovelBook findById(Long id);

    @Select("SELECT * FROM novel_book WHERE source_id = #{sourceId} AND name = #{name} AND author = #{author} LIMIT 1")
    NovelBook findBySourceNameAuthor(@Param("sourceId") Long sourceId,
                                     @Param("name") String name,
                                     @Param("author") String author);

    @Select("<script>" +
            "SELECT * FROM novel_book WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<NovelBook> findByIds(@Param("ids") List<Long> ids);

    @Insert("INSERT INTO novel_book(source_id, uploader_id, name, author, cover, intro, category, url, toc_url, status, review_status) " +
            "VALUES(#{sourceId}, #{uploaderId}, #{name}, #{author}, #{cover}, #{intro}, #{category}, #{url}, #{tocUrl}, #{status}, #{reviewStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NovelBook book);

    @Update("UPDATE novel_book SET cover=#{cover}, intro=#{intro}, status=#{status}, name=#{name}, author=#{author}, toc_url=#{tocUrl} WHERE id=#{id}")
    int update(NovelBook book);

    @Update("UPDATE novel_book SET review_status=#{reviewStatus}, review_reason=#{reason}, reviewer_id=#{reviewerId}, reviewed_at=NOW() WHERE id=#{id}")
    int updateReview(@Param("id") Long id, @Param("reviewStatus") int reviewStatus,
                     @Param("reason") String reason, @Param("reviewerId") Long reviewerId);

    @Delete("DELETE FROM novel_book WHERE id=#{id}")
    int delete(Long id);

    // ---------- 内部书库（上传并审核通过） ----------

    @Select("<script>" +
            "SELECT * FROM novel_book WHERE source_id IS NULL AND review_status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (name LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY id DESC LIMIT 50" +
            "</script>")
    List<NovelBook> searchLibrary(@Param("keyword") String keyword);

    @Select("SELECT * FROM novel_book WHERE uploader_id = #{uploaderId} ORDER BY id DESC")
    List<NovelBook> findByUploader(@Param("uploaderId") Long uploaderId);

    // ---------- 管理员审核 ----------

    @Select("<script>" +
            "SELECT * FROM novel_book WHERE source_id IS NULL " +
            "<if test='reviewStatus != null'> AND review_status = #{reviewStatus}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (name LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY review_status ASC, id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<NovelBook> findForReview(@Param("reviewStatus") Integer reviewStatus,
                                  @Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM novel_book WHERE source_id IS NULL " +
            "<if test='reviewStatus != null'> AND review_status = #{reviewStatus}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (name LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</script>")
    long countForReview(@Param("reviewStatus") Integer reviewStatus, @Param("keyword") String keyword);
}
