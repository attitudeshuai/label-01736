package com.graduate.research.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AchievementDTO {
    private Long id;
    
    @NotNull(message = "成果类型不能为空")
    private Integer type;
    
    @NotBlank(message = "成果名称不能为空")
    private String title;
    
    private String authors;
    
    private String publicationVenue;
    
    private LocalDate publicationDate;
    
    private String doiOrNumber;
    
    private String attachmentUrl;
    
    private String description;
    
    private Integer status;
}
