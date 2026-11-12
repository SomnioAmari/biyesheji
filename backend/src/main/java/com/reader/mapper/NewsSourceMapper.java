package com.reader.mapper;

import com.reader.entity.NewsSource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsSourceMapper {

    @Select("SELECT * FROM news_source WHERE id = #{id}")
    NewsSource findById(Long id);

    @Select("SELECT * FROM news_source WHERE user_id = #{userId} ORDER BY id DESC")
    List<NewsSource> findByUser(Long userId);

    @Select("SELECT * FROM news_source WHERE enabled = 1")
    List<NewsSource> findAllEnabled();

    @Insert("INSERT INTO news_source(user_id, type, name, url, category, rule_json, weight, fetch_interval, enabled) " +
            "VALUES(#{userId}, #{type}, #{name}, #{url}, #{category}, #{ruleJson}, #{weight}, #{fetchInterval}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NewsSource source);

    @Update("UPDATE news_source SET type=#{type}, name=#{name}, url=#{url}, category=#{category}, " +
            "rule_json=#{ruleJson}, weight=#{weight}, fetch_interval=#{fetchInterval}, enabled=#{enabled} " +
            "WHERE id=#{id}")
    int update(NewsSource source);

    @Delete("DELETE FROM news_source WHERE id=#{id}")
    int delete(Long id);

    @Update("UPDATE news_source SET last_fetch_at = #{time} WHERE id = #{id}")
    int updateLastFetchAt(@Param("id") Long id, @Param("time") LocalDateTime time);
}
