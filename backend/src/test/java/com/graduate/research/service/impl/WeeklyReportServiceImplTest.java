package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.WeeklyReportDTO;
import com.graduate.research.entity.User;
import com.graduate.research.entity.WeeklyReport;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.WeeklyReportMapper;
import com.graduate.research.service.UserService;
import com.graduate.research.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceImplTest {

    @Mock
    private WeeklyReportMapper weeklyReportMapper;

    @Mock
    private UserService userService;

    private WeeklyReportServiceImpl weeklyReportService;

    private User student;
    private User teacher;

    @BeforeEach
    void setUp() {
        weeklyReportService = new WeeklyReportServiceImpl();
        ReflectionTestUtils.setField(weeklyReportService, "baseMapper", weeklyReportMapper);
        ReflectionTestUtils.setField(weeklyReportService, "userService", userService);

        student = new User();
        student.setId(1L);
        student.setUsername("student1");
        student.setRealName("研究生张三");
        student.setRole(Constants.Role.STUDENT);
        student.setSupervisorId(2L);

        teacher = new User();
        teacher.setId(2L);
        teacher.setUsername("teacher1");
        teacher.setRealName("导师李四");
        teacher.setRole(Constants.Role.TEACHER);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_submit_weekly_report_when_student_submits_draft_report() {
        UserContext.setCurrentUser(student);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(1L);
        report.setYear(2025);
        report.setWeekNumber(18);
        report.setStatus(Constants.ReportStatus.DRAFT);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.submit(1L);

        assertEquals(Constants.ReportStatus.SUBMITTED, report.getStatus());
        verify(weeklyReportMapper, times(1)).updateById(report);
    }

    @Test
    void should_review_weekly_report_as_approved_when_teacher_reviews_submitted_report() {
        UserContext.setCurrentUser(teacher);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(1L);
        report.setYear(2025);
        report.setWeekNumber(18);
        report.setStatus(Constants.ReportStatus.SUBMITTED);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(1L, "内容详实，进度合理，继续保持。");

        assertEquals(Constants.ReportStatus.REVIEWED, report.getStatus());
        assertEquals("内容详实，进度合理，继续保持。", report.getSupervisorComment());
        verify(weeklyReportMapper, times(1)).updateById(report);
    }

    @Test
    void should_review_weekly_report_as_rejected_when_teacher_reviews_with_rejection_comment() {
        UserContext.setCurrentUser(teacher);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(1L);
        report.setYear(2025);
        report.setWeekNumber(18);
        report.setStatus(Constants.ReportStatus.SUBMITTED);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(1L, "内容过于简略，请补充本周具体工作内容和遇到的问题。");

        assertEquals(Constants.ReportStatus.REVIEWED, report.getStatus());
        assertEquals("内容过于简略，请补充本周具体工作内容和遇到的问题。", report.getSupervisorComment());
        verify(weeklyReportMapper, times(1)).updateById(report);
    }

    @Test
    void should_create_weekly_report_when_work_content_is_blank() {
        UserContext.setCurrentUser(student);

        WeeklyReportDTO dto = new WeeklyReportDTO();
        dto.setYear(2025);
        dto.setWeekNumber(19);
        dto.setWeekStart(LocalDate.of(2025, 5, 5));
        dto.setWeekEnd(LocalDate.of(2025, 5, 11));
        dto.setWorkContent("");
        dto.setCurrentProgress("");
        dto.setNextWeekPlan("");

        when(weeklyReportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(weeklyReportMapper.insert(any(WeeklyReport.class))).thenReturn(1);

        assertDoesNotThrow(() -> weeklyReportService.create(dto));
        verify(weeklyReportMapper, times(1)).insert(any(WeeklyReport.class));
    }

    @Test
    void should_throw_exception_when_submit_non_existent_report() {
        UserContext.setCurrentUser(student);

        when(weeklyReportMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weeklyReportService.submit(999L));
        assertEquals("周报不存在", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_submit_others_report() {
        UserContext.setCurrentUser(student);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(999L);
        report.setStatus(Constants.ReportStatus.DRAFT);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weeklyReportService.submit(1L));
        assertEquals("只能提交自己的周报", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_submit_non_draft_report() {
        UserContext.setCurrentUser(student);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setUserId(1L);
        report.setStatus(Constants.ReportStatus.SUBMITTED);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weeklyReportService.submit(1L));
        assertEquals("只能提交草稿状态的周报", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_student_reviews_report() {
        UserContext.setCurrentUser(student);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setStatus(Constants.ReportStatus.SUBMITTED);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weeklyReportService.review(1L, "评语"));
        assertEquals("学生无权审阅周报", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_review_non_submitted_report() {
        UserContext.setCurrentUser(teacher);

        WeeklyReport report = new WeeklyReport();
        report.setId(1L);
        report.setStatus(Constants.ReportStatus.DRAFT);

        when(weeklyReportMapper.selectById(1L)).thenReturn(report);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> weeklyReportService.review(1L, "评语"));
        assertEquals("只能审阅已提交的周报", exception.getMessage());
    }
}
