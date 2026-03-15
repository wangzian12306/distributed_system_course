package com.example.distributed.system.mapper;

import com.example.distributed.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    void insert(User user);
    User findById(@Param("id") Long id);
}
