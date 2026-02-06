package com.graduate.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduate.research.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User findByUsername(String username);
    
    @Select("SELECT * FROM sys_user WHERE role = 2 AND status = 1 AND deleted = 0")
    List<User> findAllTeachers();
    
    @Select("SELECT * FROM sys_user WHERE supervisor_id = #{supervisorId} AND deleted = 0")
    List<User> findStudentsBySupervisor(Long supervisorId);
}
