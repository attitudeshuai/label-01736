package com.graduate.research.controller;

import com.graduate.research.annotation.OperLog;
import com.graduate.research.common.Result;
import com.graduate.research.dto.LoginRequest;
import com.graduate.research.dto.LoginResponse;
import com.graduate.research.dto.PasswordRequest;
import com.graduate.research.entity.User;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.service.UserService;
import com.graduate.research.util.JwtUtil;
import com.graduate.research.util.PasswordUtil;
import com.graduate.research.util.UserContext;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @OperLog("用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .build();
        
        log.info("用户登录成功: {}", user.getUsername());
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出: {}", UserContext.getCurrentUsername());
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        User user = UserContext.getCurrentUser();
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("role", user.getRole());
        info.put("studentId", user.getStudentId());
        info.put("researchDirection", user.getResearchDirection());
        
        if (user.getSupervisorId() != null) {
            User supervisor = userService.getById(user.getSupervisorId());
            if (supervisor != null) {
                info.put("supervisorName", supervisor.getRealName());
            }
        }
        
        return Result.success(info);
    }

    @PutMapping("/password")
    @OperLog("修改密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordRequest request) {
        userService.changePassword(UserContext.getCurrentUserId(), request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
