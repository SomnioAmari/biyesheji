package com.reader.mapper;

import com.reader.entity.FetchLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FetchLogMapper {

    @Insert("INSERT INTO fetch_log(source_type, source_id, status, message, fetched_count, started_at, finished_at) " +
            "VALUES(#{sourceType}, #{sourceId}, #{status}, #{message}, #{fetchedCount}, #{startedAt}, #{finishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FetchLog log);

    @Select("SELECT * FROM fetch_log WHERE source_type = #{sourceType} AND source_id = #{sourceId} ORDER BY id DESC LIMIT #{limit}")
    List<FetchLog> findRecent(@Param("sourceType") int sourceType,
                              @Param("sourceId") Long sourceId,
                              @Param("limit") int limit);
}
