package com.graduate.research.controller;

import com.graduate.research.annotation.OperLog;
import com.graduate.research.common.Constants;
import com.graduate.research.common.PageResult;
import com.graduate.research.common.Result;
import com.graduate.research.dto.UserDTO;
import com.graduate.research.entity.User;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.service.UserService;
import com.graduate.research.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role) {
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() != Constants.Role.ADMIN) {
            throw new BusinessException("无权访问用户管理");
        }
        
        return Result.success(PageResult.of(userService.pageUsers(page, size, keyword, role)));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getSupervisorId() != null) {
            User supervisor = userService.getById(user.getSupervisorId());
            if (supervisor != null) {
                user.setSupervisorName(supervisor.getRealName());
            }
        }
        return Result.success(user);
    }

    @PostMapping
    @OperLog("新增用户")
    public Result<Void> create(@Valid @RequestBody UserDTO dto) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() != Constants.Role.ADMIN) {
            throw new BusinessException("无权新增用户");
        }
        userService.createUser(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog("更新用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() != Constants.Role.ADMIN) {
            throw new BusinessException("无权更新用户");
        }
        userService.updateUser(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog("删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() != Constants.Role.ADMIN) {
            throw new BusinessException("无权删除用户");
        }
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/teachers")
    public Result<List<User>> getTeachers() {
        return Result.success(userService.getTeachers());
    }

    @GetMapping("/students")
    public Result<List<User>> getStudents() {
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.TEACHER) {
            return Result.success(userService.getStudentsBySupervisor(currentUser.getId()));
        } else if (currentUser.getRole() == Constants.Role.ADMIN) {
            return Result.success(userService.list());
        }
        throw new BusinessException("无权查看学生列表");
    }
}
