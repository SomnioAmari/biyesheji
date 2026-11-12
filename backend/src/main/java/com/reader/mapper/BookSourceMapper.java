package com.reader.mapper;

import com.reader.entity.BookSource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BookSourceMapper {

    @Select("SELECT * FROM book_source WHERE id = #{id}")
    BookSource findById(Long id);

    @Select("SELECT * FROM book_source WHERE user_id = #{userId} ORDER BY id DESC")
    List<BookSource> findByUser(Long userId);

    @Select("SELECT * FROM book_source WHERE enabled = 1")
    List<BookSource> findAllEnabled();

    @Insert("INSERT INTO book_source(user_id, name, url, rule_json, enabled) VALUES(#{userId}, #{name}, #{url}, #{ruleJson}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BookSource source);

    @Update("UPDATE book_source SET name=#{name}, url=#{url}, rule_json=#{ruleJson}, enabled=#{enabled} WHERE id=#{id}")
    int update(BookSource source);

    @Delete("DELETE FROM book_source WHERE id=#{id}")
    int delete(Long id);
}
