package com.graduate.research.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    private String password;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    @NotNull(message = "角色不能为空")
    private Integer role;
    
    private Long supervisorId;
    
    private String studentId;
    
    private String researchDirection;
    
    private Integer status;
}
