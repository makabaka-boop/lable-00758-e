package com.shortvideo.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.dto.Result;
import com.shortvideo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.isValid(token)) {
                request.setAttribute("userId", jwtUtil.getUserId(token));
                return true;
            }
        }
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        try {
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或token已过期")));
        } catch (Exception e) {
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或token已过期\",\"data\":null}");
        }
        return false;
    }
}
