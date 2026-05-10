package com.shortvideo.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil 工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-256-bits-for-testing-purposes");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("生成 Token 成功")
    void generateToken_Success() {
        String token = jwtUtil.generateToken(1L, "testuser", false);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("生成 Token 后可正确解析用户ID")
    void generateToken_ParseUserId() {
        String token = jwtUtil.generateToken(123L, "testuser", false);

        Long userId = jwtUtil.getUserId(token);

        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("Token 验证 - 有效 Token")
    void isValid_ValidToken() {
        String token = jwtUtil.generateToken(1L, "testuser", false);

        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    @DisplayName("Token 验证 - 无效 Token")
    void isValid_InvalidToken() {
        assertFalse(jwtUtil.isValid("invalid-token"));
    }

    @Test
    @DisplayName("Token 验证 - null Token")
    void isValid_NullToken() {
        assertFalse(jwtUtil.isValid(null));
    }

    @Test
    @DisplayName("解析 Token - 无效 Token 返回 null")
    void parseToken_InvalidToken() {
        assertNull(jwtUtil.parseToken("invalid-token"));
    }

    @Test
    @DisplayName("解析 Token - null Token 返回 null")
    void parseToken_NullToken() {
        assertNull(jwtUtil.parseToken(null));
    }

    @Test
    @DisplayName("获取用户ID - 无效 Token 返回 null")
    void getUserId_InvalidToken() {
        assertNull(jwtUtil.getUserId("invalid-token"));
    }

    @Test
    @DisplayName("获取用户ID - null Token 返回 null")
    void getUserId_NullToken() {
        assertNull(jwtUtil.getUserId(null));
    }

    @Test
    @DisplayName("生成管理员 Token 并验证")
    void generateToken_AdminUser() {
        String token = jwtUtil.generateToken(99L, "admin", true);

        assertTrue(jwtUtil.isValid(token));
        assertEquals(99L, jwtUtil.getUserId(token));
    }
}
