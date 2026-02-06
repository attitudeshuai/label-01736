package com.graduate.research.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WeeklyReportDTO {
    private Long id;
    
    @NotNull(message = "年份不能为空")
    private Integer year;
    
    @NotNull(message = "周数不能为空")
    private Integer weekNumber;
    
    @NotNull(message = "周开始日期不能为空")
    private LocalDate weekStart;
    
    @NotNull(message = "周结束日期不能为空")
    private LocalDate weekEnd;
    
    private String workContent;
    
    private String currentProgress;
    
    private String nextWeekPlan;
}
