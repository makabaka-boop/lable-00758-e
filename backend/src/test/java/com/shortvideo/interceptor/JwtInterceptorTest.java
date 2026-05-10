package com.shortvideo.interceptor;

import com.shortvideo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtInterceptor 拦截器测试")
class JwtInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtInterceptor jwtInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("OPTIONS 请求 - 直接放行")
    void preHandle_OptionsRequest() throws Exception {
        request.setMethod("OPTIONS");

        boolean result = jwtInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(jwtUtil, never()).isValid(anyString());
    }

    @Test
    @DisplayName("无 Authorization 头 - 拦截")
    void preHandle_NoAuthorizationHeader() throws Exception {
        request.setMethod("POST");
        when(objectMapper.writeValueAsString(any(Result.class))).thenReturn("{\"code\":401,\"message\":\"未登录\"}");

        boolean result = jwtInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        verify(jwtUtil, never()).isValid(anyString());
    }

    @Test
    @DisplayName("Authorization 头格式错误 - 拦截")
    void preHandle_WrongAuthorizationFormat() throws Exception {
        request.setMethod("POST");
        request.addHeader("Authorization", "InvalidToken");
        when(objectMapper.writeValueAsString(any(Result.class))).thenReturn("{\"code\":401,\"message\":\"未登录\"}");

        boolean result = jwtInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        verify(jwtUtil, never()).isValid(anyString());
    }

    @Test
    @DisplayName("Token 无效 - 拦截")
    void preHandle_InvalidToken() throws Exception {
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer invalid-token");
        when(objectMapper.writeValueAsString(any(Result.class))).thenReturn("{\"code\":401,\"message\":\"未登录\"}");

        when(jwtUtil.isValid("invalid-token")).thenReturn(false);

        boolean result = jwtInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
        verify(jwtUtil, times(1)).isValid("invalid-token");
    }

    @Test
    @DisplayName("Token 有效 - 放行并设置 userId")
    void preHandle_ValidToken() throws Exception {
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtUtil.isValid("valid-token")).thenReturn(true);
        when(jwtUtil.getUserId("valid-token")).thenReturn(1L);

        boolean result = jwtInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(1L, request.getAttribute("userId"));
        verify(jwtUtil, times(1)).isValid("valid-token");
        verify(jwtUtil, times(1)).getUserId("valid-token");
    }
}
