package com.graduate.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduate.research.common.Constants;
import com.graduate.research.dto.WeeklyReportDTO;
import com.graduate.research.entity.User;
import com.graduate.research.entity.WeeklyReport;
import com.graduate.research.mapper.WeeklyReportMapper;
import com.graduate.research.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceImplTest {

    @Mock
    private WeeklyReportMapper weeklyReportMapper;

    @InjectMocks
    private WeeklyReportServiceImpl weeklyReportService;

    private User student;
    private User teacher;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(1L);
        student.setRole(Constants.Role.STUDENT);
        student.setRealName("张三");
        student.setSupervisorId(10L);

        teacher = new User();
        teacher.setId(10L);
        teacher.setRole(Constants.Role.TEACHER);
        teacher.setRealName("李导师");

        UserContext.setCurrentUser(student);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_set_status_to_submitted_when_student_submits_draft_report() {
        WeeklyReport report = new WeeklyReport();
        report.setId(100L);
        report.setUserId(1L);
        report.setStatus(Constants.ReportStatus.DRAFT);
        report.setYear(2026);
        report.setWeekNumber(18);

        when(weeklyReportMapper.selectById(100L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.submit(100L);

        verify(weeklyReportMapper).updateById(argThat(r ->
                r.getStatus() == Constants.ReportStatus.SUBMITTED
        ));
    }

    @Test
    void should_set_status_to_reviewed_when_teacher_approves_submitted_report() {
        UserContext.clear();
        UserContext.setCurrentUser(teacher);

        WeeklyReport report = new WeeklyReport();
        report.setId(100L);
        report.setUserId(1L);
        report.setStatus(Constants.ReportStatus.SUBMITTED);
        report.setYear(2026);
        report.setWeekNumber(18);

        when(weeklyReportMapper.selectById(100L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(100L, "完成良好");

        verify(weeklyReportMapper).updateById(argThat(r ->
                r.getStatus() == Constants.ReportStatus.REVIEWED
                        && "完成良好".equals(r.getSupervisorComment())
        ));
    }

    @Test
    void should_set_status_to_reviewed_when_teacher_rejects_with_negative_comment() {
        UserContext.clear();
        UserContext.setCurrentUser(teacher);

        WeeklyReport report = new WeeklyReport();
        report.setId(100L);
        report.setUserId(1L);
        report.setStatus(Constants.ReportStatus.SUBMITTED);
        report.setYear(2026);
        report.setWeekNumber(18);

        when(weeklyReportMapper.selectById(100L)).thenReturn(report);
        when(weeklyReportMapper.updateById(any(WeeklyReport.class))).thenReturn(1);

        weeklyReportService.review(100L, "内容不充分，请补充后重新提交");

        verify(weeklyReportMapper).updateById(argThat(r ->
                r.getStatus() == Constants.ReportStatus.REVIEWED
                        && "内容不充分，请补充后重新提交".equals(r.getSupervisorComment())
        ));
    }

    @Test
    void should_save_as_draft_when_work_content_is_blank() {
        when(weeklyReportMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(weeklyReportMapper.insert(any(WeeklyReport.class))).thenReturn(1);

        WeeklyReportDTO dto = new WeeklyReportDTO();
        dto.setYear(2026);
        dto.setWeekNumber(18);
        dto.setWeekStart(LocalDate.of(2026, 5, 4));
        dto.setWeekEnd(LocalDate.of(2026, 5, 10));
        dto.setWorkContent("");
        dto.setCurrentProgress("进行中");
        dto.setNextWeekPlan("继续实验");

        weeklyReportService.create(dto);

        verify(weeklyReportMapper).insert(argThat(r ->
                r.getStatus() == Constants.ReportStatus.DRAFT
                        && "".equals(r.getWorkContent())
                        && r.getUserId().equals(1L)
        ));
    }
}
