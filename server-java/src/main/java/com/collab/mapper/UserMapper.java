package com.collab.mapper;

import com.collab.model.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE openid = #{openid}")
    User findByOpenid(@Param("openid") String openid);

    @Select("SELECT id, nick_name, avatar_url, created_at FROM users WHERE id = #{id}")
    User findById(@Param("id") String id);

    @Insert("INSERT INTO users (id, openid, session_key, created_at) VALUES (#{id}, #{openid}, #{sessionKey}, NOW())")
    void insert(User user);

    @Update("UPDATE users SET session_key = #{sessionKey} WHERE id = #{id}")
    void updateSessionKey(@Param("id") String id, @Param("sessionKey") String sessionKey);

    @Update("UPDATE users SET nick_name = #{nickName}, avatar_url = #{avatarUrl} WHERE id = #{id}")
    void updateUserInfo(@Param("id") String id, @Param("nickName") String nickName, @Param("avatarUrl") String avatarUrl);
}
