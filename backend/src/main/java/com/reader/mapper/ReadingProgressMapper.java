package com.reader.mapper;

import com.reader.entity.ReadingProgress;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ReadingProgressMapper {

    @Select("SELECT * FROM reading_progress WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    ReadingProgress findByUserAndTarget(@Param("userId") Long userId,
                                        @Param("targetType") int targetType,
                                        @Param("targetId") Long targetId);

    @Insert("INSERT INTO reading_progress(user_id, target_type, target_id, chapter_id, scroll_position, last_read_at) " +
            "VALUES(#{userId}, #{targetType}, #{targetId}, #{chapterId}, #{scrollPosition}, NOW()) AS new " +
            "ON DUPLICATE KEY UPDATE " +
            "chapter_id = new.chapter_id, scroll_position = new.scroll_position, last_read_at = NOW()")
    int upsert(ReadingProgress progress);
}
