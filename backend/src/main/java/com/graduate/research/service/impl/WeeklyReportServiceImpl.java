package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.WeeklyReportDTO;
import com.graduate.research.entity.User;
import com.graduate.research.entity.WeeklyReport;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.WeeklyReportMapper;
import com.graduate.research.service.UserService;
import com.graduate.research.service.WeeklyReportService;
import com.graduate.research.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class WeeklyReportServiceImpl extends ServiceImpl<WeeklyReportMapper, WeeklyReport> implements WeeklyReportService {

    @Autowired
    private UserService userService;

    @Override
    public IPage<WeeklyReport> pageReports(int page, int size, Long userId, Integer year, Integer status) {
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<>();
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT) {
            wrapper.eq(WeeklyReport::getUserId, currentUser.getId());
        } else if (currentUser.getRole() == Constants.Role.TEACHER) {
            if (userId != null) {
                wrapper.eq(WeeklyReport::getUserId, userId);
            } else {
                List<User> students = userService.getStudentsBySupervisor(currentUser.getId());
                List<Long> studentIds = students.stream().map(User::getId).toList();
                studentIds = new java.util.ArrayList<>(studentIds);
                studentIds.add(currentUser.getId());
                wrapper.in(WeeklyReport::getUserId, studentIds);
            }
        } else if (userId != null) {
            wrapper.eq(WeeklyReport::getUserId, userId);
        }
        
        if (year != null) {
            wrapper.eq(WeeklyReport::getYear, year);
        }
        
        if (status != null) {
            wrapper.eq(WeeklyReport::getStatus, status);
        }
        
        wrapper.orderByDesc(WeeklyReport::getYear).orderByDesc(WeeklyReport::getWeekNumber);
        
        IPage<WeeklyReport> result = page(new Page<>(page, size), wrapper);
        
        result.getRecords().forEach(report -> {
            User user = userService.getById(report.getUserId());
            if (user != null) {
                report.setAuthorName(user.getRealName());
                if (user.getSupervisorId() != null) {
                    User supervisor = userService.getById(user.getSupervisorId());
                    if (supervisor != null) {
                        report.setSupervisorName(supervisor.getRealName());
                    }
                }
            }
        });
        
        return result;
    }

    @Override
    public WeeklyReport getDetail(Long id) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException("周报不存在");
        }
        User user = userService.getById(report.getUserId());
        if (user != null) {
            report.setAuthorName(user.getRealName());
            if (user.getSupervisorId() != null) {
                User supervisor = userService.getById(user.getSupervisorId());
                if (supervisor != null) {
                    report.setSupervisorName(supervisor.getRealName());
                }
            }
        }
        return report;
    }

    @Override
    @Transactional
    public void create(WeeklyReportDTO dto) {
        // 检查是否已存在同一周的周报
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyReport::getUserId, UserContext.getCurrentUserId())
                .eq(WeeklyReport::getYear, dto.getYear())
                .eq(WeeklyReport::getWeekNumber, dto.getWeekNumber());
        if (count(wrapper) > 0) {
            throw new BusinessException("该周的周报已存在");
        }
        
        WeeklyReport report = new WeeklyReport();
        report.setUserId(UserContext.getCurrentUserId());
        report.setYear(dto.getYear());
        report.setWeekNumber(dto.getWeekNumber());
        report.setWeekStart(dto.getWeekStart());
        report.setWeekEnd(dto.getWeekEnd());
        report.setWorkContent(dto.getWorkContent());
        report.setCurrentProgress(dto.getCurrentProgress());
        report.setNextWeekPlan(dto.getNextWeekPlan());
        report.setStatus(Constants.ReportStatus.DRAFT);
        
        save(report);
        log.info("创建周报成功: {} 第{}周", report.getYear(), report.getWeekNumber());
    }

    @Override
    @Transactional
    public void update(Long id, WeeklyReportDTO dto) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException("周报不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !report.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权修改他人的周报");
        }
        
        if (report.getStatus() == Constants.ReportStatus.REVIEWED) {
            throw new BusinessException("已审阅的周报不能修改");
        }
        
        report.setWeekStart(dto.getWeekStart());
        report.setWeekEnd(dto.getWeekEnd());
        report.setWorkContent(dto.getWorkContent());
        report.setCurrentProgress(dto.getCurrentProgress());
        report.setNextWeekPlan(dto.getNextWeekPlan());
        
        updateById(report);
        log.info("更新周报成功: {} 第{}周", report.getYear(), report.getWeekNumber());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException("周报不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT && !report.getUserId().equals(currentUser.getId())) {
            throw new BusinessException("无权删除他人的周报");
        }
        
        if (report.getStatus() != Constants.ReportStatus.DRAFT) {
            throw new BusinessException("只能删除草稿状态的周报");
        }
        
        removeById(id);
        log.info("删除周报成功: {} 第{}周", report.getYear(), report.getWeekNumber());
    }

    @Override
    @Transactional
    public void submit(Long id) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException("周报不存在");
        }
        
        if (!report.getUserId().equals(UserContext.getCurrentUserId())) {
            throw new BusinessException("只能提交自己的周报");
        }
        
        if (report.getStatus() != Constants.ReportStatus.DRAFT) {
            throw new BusinessException("只能提交草稿状态的周报");
        }
        
        report.setStatus(Constants.ReportStatus.SUBMITTED);
        updateById(report);
        log.info("提交周报成功: {} 第{}周", report.getYear(), report.getWeekNumber());
    }

    @Override
    @Transactional
    public void review(Long id, String comment) {
        WeeklyReport report = getById(id);
        if (report == null) {
            throw new BusinessException("周报不存在");
        }
        
        User currentUser = UserContext.getCurrentUser();
        if (currentUser.getRole() == Constants.Role.STUDENT) {
            throw new BusinessException("学生无权审阅周报");
        }
        
        if (report.getStatus() != Constants.ReportStatus.SUBMITTED) {
            throw new BusinessException("只能审阅已提交的周报");
        }
        
        report.setStatus(Constants.ReportStatus.REVIEWED);
        report.setSupervisorComment(comment);
        updateById(report);
        log.info("审阅周报成功: {} 第{}周", report.getYear(), report.getWeekNumber());
    }
}
