package com.graduate.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    /** 角色:1-管理员,2-导师,3-学生 */
    private Integer role;
    
    /** 导师ID */
    private Long supervisorId;
    
    /** 学号 */
    private String studentId;
    
    /** 研究方向 */
    private String researchDirection;
    
    /** 状态:0-禁用,1-启用 */
    private Integer status;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /** 导师姓名(非数据库字段) */
    @TableField(exist = false)
    private String supervisorName;
}
