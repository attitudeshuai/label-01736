package com.graduate.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("paper_reading")
public class PaperReading {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private LocalDate readingDate;
    
    private String title;
    
    private String keywords;
    
    private String source;
    
    private String authors;
    
    private String firstInstitution;
    
    private String workIntroduction;
    
    private String innovationPoints;
    
    private String thoughtsOrDrawbacks;
    
    private String attachmentUrl;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String readerName;
}
