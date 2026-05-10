package com.shortvideo.exception;

import com.shortvideo.dto.Result;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException_DefaultCode() {
        BusinessException ex = new BusinessException("操作失败");
        Result<Void> result = handler.handleBusinessException(ex);
        assertEquals(400, result.getCode());
        assertEquals("操作失败", result.getMessage());
    }

    @Test
    void handleBusinessException_CustomCode() {
        BusinessException ex = new BusinessException(403, "权限不足");
        Result<Void> result = handler.handleBusinessException(ex);
        assertEquals(403, result.getCode());
        assertEquals("权限不足", result.getMessage());
    }

    @Test
    void handleMissingParam() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("userId", "Long");
        Result<Void> result = handler.handleMissingParam(ex);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("userId"));
    }

    @Test
    void handleMaxUploadSize() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(104857600);
        Result<Void> result = handler.handleMaxUploadSize(ex);
        assertEquals(400, result.getCode());
        assertNotNull(result.getMessage());
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("unexpected error");
        Result<Void> result = handler.handleException(ex);
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }

    @Test
    void businessException_GetCode() {
        BusinessException ex1 = new BusinessException("默认400");
        assertEquals(400, ex1.getCode());

        BusinessException ex2 = new BusinessException(403, "自定义");
        assertEquals(403, ex2.getCode());
        assertEquals("自定义", ex2.getMessage());
    }

    @Test
    void businessException_Inheritance() {
        BusinessException ex = new BusinessException("test");
        assertTrue(ex instanceof RuntimeException);
    }
}
