package com.graduate.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduate.research.dto.PaperReadingDTO;
import com.graduate.research.entity.PaperReading;

public interface PaperReadingService extends IService<PaperReading> {
    
    IPage<PaperReading> pagePaperReadings(int page, int size, Long userId, String keyword);
    
    PaperReading getDetail(Long id);
    
    void create(PaperReadingDTO dto);
    
    void update(Long id, PaperReadingDTO dto);
    
    void delete(Long id);
}
