package com.graduate.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduate.research.dto.WeeklyReportDTO;
import com.graduate.research.entity.WeeklyReport;

public interface WeeklyReportService extends IService<WeeklyReport> {
    
    IPage<WeeklyReport> pageReports(int page, int size, Long userId, Integer year, Integer status);
    
    WeeklyReport getDetail(Long id);
    
    void create(WeeklyReportDTO dto);
    
    void update(Long id, WeeklyReportDTO dto);
    
    void delete(Long id);
    
    void submit(Long id);
    
    void review(Long id, String comment);
}
