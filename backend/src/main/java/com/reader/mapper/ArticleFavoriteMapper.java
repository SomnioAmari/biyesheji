package com.reader.mapper;

import com.reader.entity.ArticleFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArticleFavoriteMapper {

    @Select("SELECT * FROM article_favorite WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ArticleFavorite> findByUser(Long userId);

    @Select("SELECT * FROM article_favorite WHERE user_id = #{userId} AND article_id = #{articleId}")
    ArticleFavorite findByUserAndArticle(@Param("userId") Long userId, @Param("articleId") Long articleId);

    @Insert("INSERT INTO article_favorite(user_id, article_id) VALUES(#{userId}, #{articleId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ArticleFavorite favorite);

    @Delete("DELETE FROM article_favorite WHERE user_id = #{userId} AND article_id = #{articleId}")
    int delete(@Param("userId") Long userId, @Param("articleId") Long articleId);
}
