package com.graduate.research.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PaperReadingDTO {
    private Long id;
    
    @NotNull(message = "阅读日期不能为空")
    private LocalDate readingDate;
    
    @NotBlank(message = "论文题目不能为空")
    private String title;
    
    private String keywords;
    
    private String source;
    
    private String authors;
    
    private String firstInstitution;
    
    private String workIntroduction;
    
    private String innovationPoints;
    
    private String thoughtsOrDrawbacks;
    
    private String attachmentUrl;
}
