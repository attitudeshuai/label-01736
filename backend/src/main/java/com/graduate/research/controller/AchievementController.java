package com.graduate.research.controller;

import com.graduate.research.annotation.OperLog;
import com.graduate.research.common.PageResult;
import com.graduate.research.common.Result;
import com.graduate.research.dto.AchievementDTO;
import com.graduate.research.entity.Achievement;
import com.graduate.research.service.AchievementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @GetMapping
    public Result<PageResult<Achievement>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(achievementService.pageAchievements(page, size, userId, type, keyword)));
    }

    @GetMapping("/{id}")
    public Result<Achievement> getById(@PathVariable Long id) {
        return Result.success(achievementService.getDetail(id));
    }

    @PostMapping
    @OperLog("新增成果")
    public Result<Void> create(@Valid @RequestBody AchievementDTO dto) {
        achievementService.create(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog("更新成果")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AchievementDTO dto) {
        achievementService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog("删除成果")
    public Result<Void> delete(@PathVariable Long id) {
        achievementService.delete(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics(@RequestParam(required = false) Long userId) {
        return Result.success(achievementService.getStatistics(userId));
    }
    
    @GetMapping("/published-papers")
    public Result<List<Achievement>> getPublishedPapers() {
        return Result.success(achievementService.getPublishedPapers());
    }
}
