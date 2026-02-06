package com.graduate.research.controller;

import com.graduate.research.annotation.OperLog;
import com.graduate.research.common.PageResult;
import com.graduate.research.common.Result;
import com.graduate.research.dto.ReviewRequest;
import com.graduate.research.dto.WeeklyReportDTO;
import com.graduate.research.entity.WeeklyReport;
import com.graduate.research.service.WeeklyReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly-reports")
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @GetMapping
    public Result<PageResult<WeeklyReport>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer status) {
        return Result.success(PageResult.of(weeklyReportService.pageReports(page, size, userId, year, status)));
    }

    @GetMapping("/{id}")
    public Result<WeeklyReport> getById(@PathVariable Long id) {
        return Result.success(weeklyReportService.getDetail(id));
    }

    @PostMapping
    @OperLog("新增周报")
    public Result<Void> create(@Valid @RequestBody WeeklyReportDTO dto) {
        weeklyReportService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog("更新周报")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody WeeklyReportDTO dto) {
        weeklyReportService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog("删除周报")
    public Result<Void> delete(@PathVariable Long id) {
        weeklyReportService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/submit")
    @OperLog("提交周报")
    public Result<Void> submit(@PathVariable Long id) {
        weeklyReportService.submit(id);
        return Result.success();
    }

    @PutMapping("/{id}/review")
    @OperLog("审阅周报")
    public Result<Void> review(@PathVariable Long id, @RequestBody ReviewRequest request) {
        weeklyReportService.review(id, request.getComment());
        return Result.success();
    }
}
