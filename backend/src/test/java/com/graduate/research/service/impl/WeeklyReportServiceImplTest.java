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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceImplTest {

    @Mock
    private WeeklyReportMapper weeklyReportMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private WeeklyReportServiceImpl weeklyReportService;

    private User student;
    private User teacher;
    private WeeklyReport draftReport;
    private WeeklyReport submittedReport;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(weeklyReportService, "baseMapper", weeklyReportMapper);

        student = new User();
        student.setId(1L);
        student.setUsername("student1");
        student.setRealName("张三");
        student.setRole(Constants.Role.STUDENT);
        student.setSupervisorId(10L);

        teacher = new User();
        teacher.setId(10L);
        teacher.setUsername("teacher1");
        teacher.setRealName("李导师");
        teacher.setRole(Constants.Role.TEACHER);

        draftReport = new WeeklyReport();
        draftReport.setId(100L);
        draftReport.setUserId(1L);
        draftReport.setYear(2026);
        draftReport.setWeekNumber(18);
        draftReport.setWeekStart(LocalDate.of(2026, 4, 27));
        draftReport.setWeekEnd(LocalDate.of(2026, 5, 3));
        draftReport.setWorkContent("完成了文献综述");
        draftReport.setCurrentProgress("进度正常");
        draftReport.setNextWeekPlan("开始实验设计");
        draftReport.setStatus(Constants.ReportStatus.DRAFT);

        submittedReport = new WeeklyReport();
        submittedReport.setId(200L);
        submittedReport.setUserId(1L);
        submittedReport.setYear(2026);
        submittedReport.setWeekNumber(17);
        submittedReport.setWeekStart(LocalDate.of(2026, 4, 20));
        submittedReport.setWeekEnd(LocalDate.of(2026, 4, 26));
        submittedReport.setWorkContent("完成了开题报告");
        submittedReport.setCurrentProgress("进度正常");
        submittedReport.setNextWeekPlan("修改开题报告");
        submittedReport.setStatus(Constants.ReportStatus.SUBMITTED);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_submitSuccessfully_when_studentSubmitsOwnDraftReport() {
        UserContext.setCurrentUser(student);

        when(weeklyReportMapper.selectById(100L)).thenReturn(draftReport);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.submit(100L);

        verify(weeklyReportMapper).updateById(argThat(report ->
                report.getStatus().equals(Constants.ReportStatus.SUBMITTED)
        ));
    }

    @Test
    void should_throwBusinessException_when_studentSubmitsOthersReport() {
        UserContext.setCurrentUser(student);

        WeeklyReport othersReport = new WeeklyReport();
        othersReport.setId(300L);
        othersReport.setUserId(999L);
        othersReport.setStatus(Constants.ReportStatus.DRAFT);

        when(weeklyReportMapper.selectById(300L)).thenReturn(othersReport);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> weeklyReportService.submit(300L));
        assertEquals("只能提交自己的周报", ex.getMessage());
        verify(weeklyReportMapper, never()).updateById(any());
    }

    @Test
    void should_throwBusinessException_when_submittingNonDraftReport() {
        UserContext.setCurrentUser(student);

        submittedReport.setUserId(1L);
        when(weeklyReportMapper.selectById(200L)).thenReturn(submittedReport);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> weeklyReportService.submit(200L));
        assertEquals("只能提交草稿状态的周报", ex.getMessage());
    }

    @Test
    void should_reviewApprove_when_teacherReviewsSubmittedReport() {
        UserContext.setCurrentUser(teacher);

        when(weeklyReportMapper.selectById(200L)).thenReturn(submittedReport);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(200L, "内容详实，继续努力");

        verify(weeklyReportMapper).updateById(argThat(report ->
                report.getStatus().equals(Constants.ReportStatus.REVIEWED)
                        && "内容详实，继续努力".equals(report.getSupervisorComment())
        ));
    }

    @Test
    void should_reviewReject_when_teacherReviewsWithRejectComment() {
        UserContext.setCurrentUser(teacher);

        when(weeklyReportMapper.selectById(200L)).thenReturn(submittedReport);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(200L, "内容不够详细，请补充后重新提交");

        verify(weeklyReportMapper).updateById(argThat(report ->
                report.getStatus().equals(Constants.ReportStatus.REVIEWED)
                        && "内容不够详细，请补充后重新提交".equals(report.getSupervisorComment())
        ));
    }

    @Test
    void should_throwBusinessException_when_studentTriesToReview() {
        UserContext.setCurrentUser(student);

        when(weeklyReportMapper.selectById(200L)).thenReturn(submittedReport);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> weeklyReportService.review(200L, "评语"));
        assertEquals("学生无权审阅周报", ex.getMessage());
        verify(weeklyReportMapper, never()).updateById(any());
    }

    @Test
    void should_throwBusinessException_when_reviewingNonSubmittedReport() {
        UserContext.setCurrentUser(teacher);

        when(weeklyReportMapper.selectById(100L)).thenReturn(draftReport);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> weeklyReportService.review(100L, "评语"));
        assertEquals("只能审阅已提交的周报", ex.getMessage());
        verify(weeklyReportMapper, never()).updateById(any());
    }

    @Test
    void should_createSuccessfully_when_workContentIsBlank() {
        UserContext.setCurrentUser(student);

        when(weeklyReportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(weeklyReportMapper.insert(any(WeeklyReport.class))).thenReturn(1);

        WeeklyReportDTO dto = new WeeklyReportDTO();
        dto.setYear(2026);
        dto.setWeekNumber(19);
        dto.setWeekStart(LocalDate.of(2026, 5, 4));
        dto.setWeekEnd(LocalDate.of(2026, 5, 10));
        dto.setWorkContent("");
        dto.setCurrentProgress("暂无进展");
        dto.setNextWeekPlan("下周开始");

        assertDoesNotThrow(() -> weeklyReportService.create(dto));

        verify(weeklyReportMapper).insert(argThat(report ->
                "".equals(report.getWorkContent())
                        && report.getStatus().equals(Constants.ReportStatus.DRAFT)
        ));
    }
}
