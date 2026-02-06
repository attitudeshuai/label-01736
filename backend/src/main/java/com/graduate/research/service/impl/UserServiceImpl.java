package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.UserDTO;
import com.graduate.research.entity.User;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.UserMapper;
import com.graduate.research.service.UserService;
import com.graduate.research.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        return baseMapper.findByUsername(username);
    }

    @Override
    public IPage<User> pageUsers(int page, int size, String keyword, Integer role) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        
        IPage<User> result = page(new Page<>(page, size), wrapper);
        
        // 填充导师姓名
        result.getRecords().forEach(user -> {
            if (user.getSupervisorId() != null) {
                User supervisor = getById(user.getSupervisorId());
                if (supervisor != null) {
                    user.setSupervisorName(supervisor.getRealName());
                }
            }
        });
        
        return result;
    }

    @Override
    @Transactional
    public void createUser(UserDTO dto) {
        // 检查用户名是否存在
        User existing = findByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setSupervisorId(dto.getSupervisorId());
        user.setStudentId(dto.getStudentId());
        user.setResearchDirection(dto.getResearchDirection());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.UserStatus.ENABLED);
        
        save(user);
        log.info("创建用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserDTO dto) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(dto.getUsername())) {
            User existing = findByUsername(dto.getUsername());
            if (existing != null) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }
        
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(PasswordUtil.encode(dto.getPassword()));
        }
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setSupervisorId(dto.getSupervisorId());
        user.setStudentId(dto.getStudentId());
        user.setResearchDirection(dto.getResearchDirection());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        
        updateById(user);
        log.info("更新用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() == Constants.Role.ADMIN) {
            throw new BusinessException("不能删除管理员账号");
        }
        removeById(id);
        log.info("删除用户成功: {}", user.getUsername());
    }

    @Override
    public List<User> getTeachers() {
        return baseMapper.findAllTeachers();
    }

    @Override
    public List<User> getStudentsBySupervisor(Long supervisorId) {
        return baseMapper.findStudentsBySupervisor(supervisorId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        updateById(user);
        log.info("用户修改密码成功: {}", user.getUsername());
    }
}
