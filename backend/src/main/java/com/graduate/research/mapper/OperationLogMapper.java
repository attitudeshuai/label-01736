package com.graduate.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graduate.research.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
