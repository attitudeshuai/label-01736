package com.graduate.research.controller;

import com.graduate.research.annotation.OperLog;
import com.graduate.research.common.PageResult;
import com.graduate.research.common.Result;
import com.graduate.research.dto.PaperReadingDTO;
import com.graduate.research.entity.PaperReading;
import com.graduate.research.service.PaperReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paper-readings")
public class PaperReadingController {

    @Autowired
    private PaperReadingService paperReadingService;

    @GetMapping
    public Result<PageResult<PaperReading>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(paperReadingService.pagePaperReadings(page, size, userId, keyword)));
    }

    @GetMapping("/{id}")
    public Result<PaperReading> getById(@PathVariable Long id) {
        return Result.success(paperReadingService.getDetail(id));
    }

    @PostMapping
    @OperLog("新增论文阅读记录")
    public Result<Void> create(@Valid @RequestBody PaperReadingDTO dto) {
        paperReadingService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog("更新论文阅读记录")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PaperReadingDTO dto) {
        paperReadingService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog("删除论文阅读记录")
    public Result<Void> delete(@PathVariable Long id) {
        paperReadingService.delete(id);
        return Result.success();
    }
}
