package com.graduate.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("weekly_report")
public class WeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Integer year;
    
    private Integer weekNumber;
    
    private LocalDate weekStart;
    
    private LocalDate weekEnd;
    
    /** 本周工作内容(JSON格式) */
    private String workContent;
    
    private String currentProgress;
    
    private String nextWeekPlan;
    
    /** 状态:1-草稿,2-已提交,3-已审阅 */
    private Integer status;
    
    private String supervisorComment;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private String authorName;
    
    @TableField(exist = false)
    private String supervisorName;
}
