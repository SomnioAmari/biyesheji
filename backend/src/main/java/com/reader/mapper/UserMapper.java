package com.reader.mapper;

import com.reader.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper {

    @Select("SELECT * FROM `user` WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM `user` WHERE id = #{id}")
    User findById(Long id);

    @Insert("INSERT INTO `user`(username, password, nickname, email, role, status) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{email}, #{role}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("<script>" +
            "SELECT * FROM `user` " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "ORDER BY id DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<User> findPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("size") int size);

    @Select("<script>" +
            "SELECT COUNT(*) FROM `user` " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    (username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "</where>" +
            "</script>")
    long count(@Param("keyword") String keyword);

    @Update("UPDATE `user` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    @Update("UPDATE `user` SET role = #{role} WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") int role);

    @Update("UPDATE `user` SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
