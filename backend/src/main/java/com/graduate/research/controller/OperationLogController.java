package com.graduate.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.graduate.research.common.Constants;
import com.graduate.research.common.PageResult;
import com.graduate.research.common.Result;
import com.graduate.research.entity.OperationLog;
import com.graduate.research.exception.BusinessException;
import com.graduate.research.mapper.OperationLogMapper;
import com.graduate.research.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @GetMapping
    public Result<PageResult<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        
        if (UserContext.getCurrentRole() != Constants.Role.ADMIN) {
            throw new BusinessException("无权查看操作日志");
        }
        
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(OperationLog::getOperation, keyword)
                    .or().like(OperationLog::getUsername, keyword);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        
        Page<OperationLog> result = operationLogMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(PageResult.of(result));
    }
}
