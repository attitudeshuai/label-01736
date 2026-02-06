package com.graduate.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduate.research.dto.AchievementDTO;
import com.graduate.research.entity.Achievement;

import java.util.List;
import java.util.Map;

public interface AchievementService extends IService<Achievement> {
    
    IPage<Achievement> pageAchievements(int page, int size, Long userId, Integer type, String keyword);
    
    Achievement getDetail(Long id);
    
    void create(AchievementDTO dto);
    
    void update(Long id, AchievementDTO dto);
    
    void delete(Long id);
    
    Map<String, Object> getStatistics(Long userId);
    
    List<Achievement> getPublishedPapers();
}
