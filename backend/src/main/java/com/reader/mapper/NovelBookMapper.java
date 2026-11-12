package com.reader.mapper;

import com.reader.entity.NovelBook;
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

    @Insert("INSERT INTO novel_book(source_id, name, author, cover, intro, url, toc_url, status) " +
            "VALUES(#{sourceId}, #{name}, #{author}, #{cover}, #{intro}, #{url}, #{tocUrl}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NovelBook book);

    @Update("UPDATE novel_book SET cover=#{cover}, intro=#{intro}, status=#{status}, name=#{name}, author=#{author}, toc_url=#{tocUrl} WHERE id=#{id}")
    int update(NovelBook book);
}
