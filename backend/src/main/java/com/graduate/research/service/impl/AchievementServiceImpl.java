package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.AchievementDTO;
import com.graduate.research.entity.Achievement;
import com.graduate.research.entity.User;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.AchievementMapper;
import com.graduate.research.service.AchievementService;
import com.graduate.research.service.UserService;
import com.graduate.research.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AchievementServiceImpl extends ServiceImpl<AchievementMapper, Achievement> implements AchievementService {

    @Autowired
    private UserService userService;

    @Override
    public IPage<Achievement> pageAchievements(int page, int size, Long userId, Integer type, String keyword) {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT) {
            wrapper.eq(Achievement::getUserId, currentUser.getId());
        } else if (currentUser.getRole() == Constants.Role.TEACHER) {
            if (userId != null) {
                wrapper.eq(Achievement::getUserId, userId);
            } else {
                List<User> students = userService.getStudentsBySupervisor(currentUser.getId());
                List<Long> studentIds = students.stream().map(User::getId).toList();
                studentIds = new java.util.ArrayList<>(studentIds);
                studentIds.add(currentUser.getId());
                wrapper.in(Achievement::getUserId, studentIds);
            }
        } else if (userId != null) {
            wrapper.eq(Achievement::getUserId, userId);
        }
        
        if (type != null) {
            wrapper.eq(Achievement::getType, type);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Achievement::getTitle, keyword)
                    .or().like(Achievement::getAuthors, keyword));
        }
        
        wrapper.orderByDesc(Achievement::getPublicationDate);
        
        IPage<Achievement> result = page(new Page<>(page, size), wrapper);
        
        result.getRecords().forEach(a -> {
            User user = userService.getById(a.getUserId());
            if (user != null) {
                a.setOwnerName(user.getRealName());
            }
        });
        
        return result;
    }

    @Override
    public Achievement getDetail(Long id) {
        Achievement achievement = getById(id);
        if (achievement == null) {
            throw new BusinessException("成果不存在");
        }
        User user = userService.getById(achievement.getUserId());
        if (user != null) {
            achievement.setOwnerName(user.getRealName());
        }
        return achievement;
    }

    @Override
    @Transactional
    public void create(AchievementDTO dto) {
        Achievement achievement = new Achievement();
        achievement.setUserId(UserContext.getCurrentUserId());
        achievement.setType(dto.getType());
        achievement.setTitle(dto.getTitle());
        achievement.setAuthors(dto.getAuthors());
        achievement.setPublicationVenue(dto.getPublicationVenue());
        achievement.setPublicationDate(dto.getPublicationDate());
        achievement.setDoiOrNumber(dto.getDoiOrNumber());
        achievement.setAttachmentUrl(dto.getAttachmentUrl());
        achievement.setDescription(dto.getDescription());
        achievement.setStatus(dto.getStatus() != null ? dto.getStatus() : Constants.AchievementStatus.PUBLISHED);
        
        save(achievement);
        log.info("创建成果成功: {}", achievement.getTitle());
    }

    @Override
    @Transactional
    public void update(Long id, AchievementDTO dto) {
        Achievement achievement = getById(id);
        if (achievement == null) {
            throw new BusinessException("成果不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !achievement.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权修改他人的成果");
        }
        
        achievement.setType(dto.getType());
        achievement.setTitle(dto.getTitle());
        achievement.setAuthors(dto.getAuthors());
        achievement.setPublicationVenue(dto.getPublicationVenue());
        achievement.setPublicationDate(dto.getPublicationDate());
        achievement.setDoiOrNumber(dto.getDoiOrNumber());
        achievement.setAttachmentUrl(dto.getAttachmentUrl());
        achievement.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            achievement.setStatus(dto.getStatus());
        }
        
        updateById(achievement);
        log.info("更新成果成功: {}", achievement.getTitle());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Achievement achievement = getById(id);
        if (achievement == null) {
            throw new BusinessException("成果不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !achievement.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除他人的成果");
        }
        
        removeById(id);
        log.info("删除成果成功: {}", achievement.getTitle());
    }

    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        
        User currentUser = UserContext.getCurrentUser();
        if (userId != null) {
            wrapper.eq(Achievement::getUserId, userId);
        } else if (currentUser.getRole() == Constants.Role.STUDENT) {
            wrapper.eq(Achievement::getUserId, currentUser.getId());
        } else if (currentUser.getRole() == Constants.Role.TEACHER) {
            List<User> students = userService.getStudentsBySupervisor(currentUser.getId());
            List<Long> studentIds = students.stream().map(User::getId).toList();
            studentIds = new java.util.ArrayList<>(studentIds);
            studentIds.add(currentUser.getId());
            wrapper.in(Achievement::getUserId, studentIds);
        }
        
        List<Achievement> achievements = list(wrapper);
        
        long paperCount = achievements.stream().filter(a -> a.getType() == Constants.AchievementType.PAPER).count();
        long patentCount = achievements.stream().filter(a -> a.getType() == Constants.AchievementType.PATENT).count();
        long softwareCount = achievements.stream().filter(a -> a.getType() == Constants.AchievementType.SOFTWARE).count();
        
        stats.put("total", achievements.size());
        stats.put("paperCount", paperCount);
        stats.put("patentCount", patentCount);
        stats.put("softwareCount", softwareCount);
        
        return stats;
    }
    
    @Override
    public List<Achievement> getPublishedPapers() {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getType, Constants.AchievementType.PAPER)
               .eq(Achievement::getStatus, Constants.AchievementStatus.PUBLISHED)
               .orderByDesc(Achievement::getPublicationDate);
        
        List<Achievement> papers = list(wrapper);
        papers.forEach(a -> {
            User user = userService.getById(a.getUserId());
            if (user != null) {
                a.setOwnerName(user.getRealName());
            }
        });
        
        return papers;
    }
}
