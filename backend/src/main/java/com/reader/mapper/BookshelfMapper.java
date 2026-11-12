package com.reader.mapper;

import com.reader.entity.Bookshelf;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BookshelfMapper {

    @Select("SELECT * FROM bookshelf WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Bookshelf> findByUser(Long userId);

    @Select("SELECT * FROM bookshelf WHERE user_id = #{userId} AND book_id = #{bookId}")
    Bookshelf findByUserAndBook(@Param("userId") Long userId, @Param("bookId") Long bookId);

    @Insert("INSERT INTO bookshelf(user_id, book_id) VALUES(#{userId}, #{bookId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Bookshelf bookshelf);

    @Delete("DELETE FROM bookshelf WHERE user_id = #{userId} AND book_id = #{bookId}")
    int delete(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
