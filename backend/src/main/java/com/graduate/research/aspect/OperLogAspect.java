package com.graduate.research.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduate.research.annotation.OperLog;
import com.graduate.research.entity.OperationLog;
import com.graduate.research.mapper.OperationLogMapper;
import com.graduate.research.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class OperLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog logEntity = new OperationLog();
        
        try {
            Object result = point.proceed();
            logEntity.setStatus(1);
            return result;
        } catch (Exception e) {
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            saveLog(point, operLog, logEntity, costTime);
        }
    }

    private void saveLog(ProceedingJoinPoint point, OperLog operLog, OperationLog logEntity, long costTime) {
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            String className = point.getTarget().getClass().getName();
            String methodName = signature.getName();

            logEntity.setOperation(operLog.value());
            logEntity.setMethod(className + "." + methodName + "()");
            logEntity.setCostTime(costTime);

            if (UserContext.getCurrentUser() != null) {
                logEntity.setUserId(UserContext.getCurrentUserId());
                logEntity.setUsername(UserContext.getCurrentUsername());
            }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setIp(getIpAddress(request));
                
                Object[] args = point.getArgs();
                if (args != null && args.length > 0) {
                    String params = objectMapper.writeValueAsString(args);
                    logEntity.setParams(params.length() > 2000 ? params.substring(0, 2000) : params);
                }
            }

            operationLogMapper.insert(logEntity);
            log.info("操作日志: {} - {} - {}ms", operLog.value(), logEntity.getStatus() == 1 ? "成功" : "失败", costTime);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
