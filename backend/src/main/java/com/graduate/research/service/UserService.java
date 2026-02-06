package com.graduate.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduate.research.dto.UserDTO;
import com.graduate.research.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    
    User findByUsername(String username);
    
    IPage<User> pageUsers(int page, int size, String keyword, Integer role);
    
    void createUser(UserDTO dto);
    
    void updateUser(Long id, UserDTO dto);
    
    void deleteUser(Long id);
    
    List<User> getTeachers();
    
    List<User> getStudentsBySupervisor(Long supervisorId);
    
    void changePassword(Long userId, String oldPassword, String newPassword);
}
