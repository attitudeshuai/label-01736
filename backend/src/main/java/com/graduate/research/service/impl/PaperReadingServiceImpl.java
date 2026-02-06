package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.PaperReadingDTO;
import com.graduate.research.entity.PaperReading;
import com.graduate.research.entity.User;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.PaperReadingMapper;
import com.graduate.research.service.PaperReadingService;
import com.graduate.research.service.UserService;
import com.graduate.research.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class PaperReadingServiceImpl extends ServiceImpl<PaperReadingMapper, PaperReading> implements PaperReadingService {

    @Autowired
    private UserService userService;

    @Override
    public IPage<PaperReading> pagePaperReadings(int page, int size, Long userId, String keyword) {
        LambdaQueryWrapper<PaperReading> wrapper = new LambdaQueryWrapper<>();
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT) {
            // 学生只能看自己的
            wrapper.eq(PaperReading::getUserId, currentUser.getId());
        } else if (currentUser.getRole() == Constants.Role.TEACHER) {
            // 导师可以看自己学生的
            if (userId != null) {
                wrapper.eq(PaperReading::getUserId, userId);
            } else {
                List<User> students = userService.getStudentsBySupervisor(currentUser.getId());
                List<Long> studentIds = students.stream().map(User::getId).toList();
                studentIds = new java.util.ArrayList<>(studentIds);
                studentIds.add(currentUser.getId());
                wrapper.in(PaperReading::getUserId, studentIds);
            }
        } else if (userId != null) {
            wrapper.eq(PaperReading::getUserId, userId);
        }
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(PaperReading::getTitle, keyword)
                    .or().like(PaperReading::getKeywords, keyword)
                    .or().like(PaperReading::getAuthors, keyword));
        }
        
        wrapper.orderByDesc(PaperReading::getReadingDate);
        
        IPage<PaperReading> result = page(new Page<>(page, size), wrapper);
        
        // 填充阅读人姓名
        result.getRecords().forEach(pr -> {
            User user = userService.getById(pr.getUserId());
            if (user != null) {
                pr.setReaderName(user.getRealName());
            }
        });
        
        return result;
    }

    @Override
    public PaperReading getDetail(Long id) {
        PaperReading pr = getById(id);
        if (pr == null) {
            throw new BusinessException("论文阅读记录不存在");
        }
        User user = userService.getById(pr.getUserId());
        if (user != null) {
            pr.setReaderName(user.getRealName());
        }
        return pr;
    }

    @Override
    @Transactional
    public void create(PaperReadingDTO dto) {
        PaperReading pr = new PaperReading();
        pr.setUserId(UserContext.getCurrentUserId());
        pr.setReadingDate(dto.getReadingDate());
        pr.setTitle(dto.getTitle());
        pr.setKeywords(dto.getKeywords());
        pr.setSource(dto.getSource());
        pr.setAuthors(dto.getAuthors());
        pr.setFirstInstitution(dto.getFirstInstitution());
        pr.setWorkIntroduction(dto.getWorkIntroduction());
        pr.setInnovationPoints(dto.getInnovationPoints());
        pr.setThoughtsOrDrawbacks(dto.getThoughtsOrDrawbacks());
        pr.setAttachmentUrl(dto.getAttachmentUrl());
        
        save(pr);
        log.info("创建论文阅读记录成功: {}", pr.getTitle());
    }

    @Override
    @Transactional
    public void update(Long id, PaperReadingDTO dto) {
        PaperReading pr = getById(id);
        if (pr == null) {
            throw new BusinessException("论文阅读记录不存在");
        }
        
        // 检查权限
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !pr.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权修改他人的记录");
        }
        
        pr.setReadingDate(dto.getReadingDate());
        pr.setTitle(dto.getTitle());
        pr.setKeywords(dto.getKeywords());
        pr.setSource(dto.getSource());
        pr.setAuthors(dto.getAuthors());
        pr.setFirstInstitution(dto.getFirstInstitution());
        pr.setWorkIntroduction(dto.getWorkIntroduction());
        pr.setInnovationPoints(dto.getInnovationPoints());
        pr.setThoughtsOrDrawbacks(dto.getThoughtsOrDrawbacks());
        pr.setAttachmentUrl(dto.getAttachmentUrl());
        
        updateById(pr);
        log.info("更新论文阅读记录成功: {}", pr.getTitle());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PaperReading pr = getById(id);
        if (pr == null) {
            throw new BusinessException("论文阅读记录不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !pr.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除他人的记录");
        }
        
        removeById(id);
        log.info("删除论文阅读记录成功: {}", pr.getTitle());
    }
}
