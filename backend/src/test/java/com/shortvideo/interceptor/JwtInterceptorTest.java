package com.shortvideo.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortvideo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void preHandle_OptionsRequest_Pass() throws Exception {
        request.setMethod("OPTIONS");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    void preHandle_ValidToken_Pass() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtUtil.isValid("valid-token")).thenReturn(true);
        when(jwtUtil.getUserId("valid-token")).thenReturn(1L);

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertTrue(result);
        assertEquals(1L, request.getAttribute("userId"));
    }

    @Test
    void preHandle_InvalidToken_Reject() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtUtil.isValid("invalid-token")).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_NoToken_Reject() throws Exception {
        request.setMethod("GET");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_TokenWithoutBearerPrefix_Reject() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "raw-token-without-bearer");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_EmptyAuthorizationHeader_Reject() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_BearerWithEmptyToken_Reject() throws Exception {
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer ");

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        boolean result = jwtInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_ResponseContentType_Set() throws Exception {
        request.setMethod("GET");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":401}");

        jwtInterceptor.preHandle(request, response, null);

        assertTrue(response.getContentType().contains("application/json"));
        assertTrue(response.getContentType().contains("UTF-8"));
    }
}
