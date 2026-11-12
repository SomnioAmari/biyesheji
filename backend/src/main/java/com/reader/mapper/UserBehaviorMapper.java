package com.reader.mapper;

import com.reader.entity.UserBehavior;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserBehaviorMapper {

    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    UserBehavior findByUserAndTarget(@Param("userId") Long userId,
                                     @Param("targetType") int targetType,
                                     @Param("targetId") Long targetId);

    @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND target_type = #{targetType}")
    List<UserBehavior> findByUserAndType(@Param("userId") Long userId, @Param("targetType") int targetType);

    @Insert("INSERT INTO user_behavior(user_id, target_type, target_id, click_count, read_duration, read_complete, last_action_at) " +
            "VALUES(#{userId}, #{targetType}, #{targetId}, #{clickCount}, #{readDuration}, #{readComplete}, NOW()) AS new " +
            "ON DUPLICATE KEY UPDATE " +
            "click_count = user_behavior.click_count + new.click_count, " +
            "read_duration = user_behavior.read_duration + new.read_duration, " +
            "read_complete = GREATEST(user_behavior.read_complete, new.read_complete), " +
            "last_action_at = NOW()")
    int upsert(UserBehavior behavior);
}
