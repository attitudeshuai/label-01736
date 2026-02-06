package com.graduate.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    /** 类型:1-论文,2-专利,3-软著 */
    private Integer type;
    
    private String title;
    
    private String authors;
    
    private String publicationVenue;
    
    private LocalDate publicationDate;
    
    private String doiOrNumber;
    
    private String attachmentUrl;
    
    private String description;
    
    /** 状态:1-已发表/授权,2-申请中 */
    private Integer status;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String ownerName;
}
