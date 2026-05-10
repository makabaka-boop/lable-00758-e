package com.shortvideo.exception;

import com.shortvideo.dto.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 全局异常处理测试")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("处理业务异常 - BusinessException")
    void handleBusinessException() {
        BusinessException e = new BusinessException(400, "业务异常信息");

        Result<Void> result = exceptionHandler.handleBusinessException(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertEquals("业务异常信息", result.getMessage());
    }

    @Test
    @DisplayName("处理参数校验异常 - MethodArgumentNotValidException")
    void handleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "user",
                "username",
                "用户名不能为空"
        );
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);

        Result<Void> result = exceptionHandler.handleValidationException(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("username"));
    }

    @Test
    @DisplayName("处理参数绑定异常 - BindException")
    void handleBindException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "video",
                "title",
                "标题不能为空"
        );
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        BindException e = new BindException(bindingResult);

        Result<Void> result = exceptionHandler.handleBindException(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("title"));
    }

    @Test
    @DisplayName("处理约束校验异常 - ConstraintViolationException")
    void handleConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("userId");
        when(violation.getMessage()).thenReturn("用户ID不能为空");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException e = new ConstraintViolationException(violations);

        Result<Void> result = exceptionHandler.handleConstraintViolation(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("userId"));
    }

    @Test
    @DisplayName("处理缺少参数异常 - MissingServletRequestParameterException")
    void handleMissingParam() {
        MissingServletRequestParameterException e = new MissingServletRequestParameterException("file", "MultipartFile");

        Result<Void> result = exceptionHandler.handleMissingParam(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("file"));
    }

    @Test
    @DisplayName("处理文件上传过大异常 - MaxUploadSizeExceededException")
    void handleMaxUploadSize() {
        org.springframework.web.multipart.MaxUploadSizeExceededException e =
                new org.springframework.web.multipart.MaxUploadSizeExceededException(1000000);

        Result<Void> result = exceptionHandler.handleMaxUploadSize(e);

        assertNotNull(result);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("100MB"));
    }

    @Test
    @DisplayName("处理未知系统异常 - Exception")
    void handleException() {
        Exception e = new RuntimeException("未知错误");

        Result<Void> result = exceptionHandler.handleException(e);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("系统繁忙，请稍后重试", result.getMessage());
    }
}
