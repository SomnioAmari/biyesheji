package com.reader.mapper;

import com.reader.entity.Article;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleMapper {

    @Select("SELECT * FROM article WHERE id = #{id}")
    Article findById(Long id);

    @Select("SELECT * FROM article WHERE source_id = #{sourceId} AND url_hash = #{urlHash}")
    Article findBySourceAndUrlHash(@Param("sourceId") Long sourceId, @Param("urlHash") String urlHash);

    @Insert("INSERT INTO article(source_id, title, author, source_name, url, url_hash, summary, content, cover, published_at) " +
            "VALUES(#{sourceId}, #{title}, #{author}, #{sourceName}, #{url}, #{urlHash}, #{summary}, #{content}, #{cover}, #{publishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Article article);

    @Select("SELECT a.* FROM article a JOIN news_source s ON a.source_id = s.id " +
            "WHERE s.user_id = #{userId} AND s.enabled = 1 " +
            "ORDER BY a.published_at DESC LIMIT #{limit}")
    List<Article> findRecentByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT a.* FROM article a JOIN news_source s ON a.source_id = s.id " +
            "WHERE s.user_id = #{userId} AND s.enabled = 1 " +
            "<if test='sourceId != null'> AND a.source_id = #{sourceId}</if> " +
            "ORDER BY a.published_at DESC LIMIT #{limit}" +
            "</script>")
    List<Article> findRecentByUserAndSource(@Param("userId") Long userId,
                                            @Param("sourceId") Long sourceId,
                                            @Param("limit") int limit);

    @Select("<script>" +
            "SELECT * FROM article WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Article> findByIds(@Param("ids") List<Long> ids);
}
