package com.graduate.research.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
}
