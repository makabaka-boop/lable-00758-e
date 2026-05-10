package com.shortvideo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.annotation.Log;
import com.shortvideo.entity.OperationLog;
import com.shortvideo.repository.OperationLogRepository;
import com.shortvideo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private OperationLogRepository logRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint point, Log log) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = point.proceed();
        long duration = System.currentTimeMillis() - start;
        
        try {
            saveLog(point, log, duration);
        } catch (Exception e) {
            // 日志保存失败不影响业务
        }
        
        return result;
    }
    
    private void saveLog(ProceedingJoinPoint point, Log log, long duration) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        
        HttpServletRequest request = attrs.getRequest();
        OperationLog opLog = new OperationLog();
        
        // 从token获取用户信息
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parseToken(token.substring(7));
            if (claims != null) {
                opLog.setUserId(Long.valueOf(claims.getSubject()));
                opLog.setUsername((String) claims.get("username"));
            }
        }
        
        opLog.setOperation(log.value());
        opLog.setMethod(point.getSignature().toString());
        opLog.setIp(getIpAddr(request));
        opLog.setDuration(duration);
        
        try {
            Object[] args = point.getArgs();
            if (args.length > 0) {
                String params = objectMapper.writeValueAsString(args);
                opLog.setParams(params.length() > 500 ? params.substring(0, 500) : params);
            }
        } catch (Exception ignored) {}
        
        logRepository.save(opLog);
    }
    
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
