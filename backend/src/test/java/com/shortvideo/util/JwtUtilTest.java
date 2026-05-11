package com.shortvideo.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-256-bits-for-hs256-algorithm");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        jwtUtil.init();
    }

    @Test
    @DisplayName("生成Token成功")
    void generateToken_Success() {
        String token = jwtUtil.generateToken(1L, "testuser", true);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("解析Token成功")
    void parseToken_Success() {
        String token = jwtUtil.generateToken(1L, "testuser", true);

        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("testuser", claims.get("username"));
        assertEquals(true, claims.get("isAdmin"));
    }

    @Test
    @DisplayName("解析Token失败 - Token格式错误")
    void parseToken_InvalidToken() {
        Claims claims = jwtUtil.parseToken("invalid-token");

        assertNull(claims);
    }

    @Test
    @DisplayName("获取用户ID成功")
    void getUserId_Success() {
        String token = jwtUtil.generateToken(123L, "testuser", false);

        Long userId = jwtUtil.getUserId(token);

        assertNotNull(userId);
        assertEquals(123L, userId);
    }

    @Test
    @DisplayName("获取用户ID失败 - Token无效")
    void getUserId_InvalidToken() {
        Long userId = jwtUtil.getUserId("invalid-token");

        assertNull(userId);
    }

    @Test
    @DisplayName("验证Token有效")
    void isValid_ValidToken() {
        String token = jwtUtil.generateToken(1L, "testuser", true);

        boolean valid = jwtUtil.isValid(token);

        assertTrue(valid);
    }

    @Test
    @DisplayName("验证Token失败 - Token无效")
    void isValid_InvalidToken() {
        boolean valid = jwtUtil.isValid("invalid-token");

        assertFalse(valid);
    }

    @Test
    @DisplayName("验证Token失败 - Token为空")
    void isValid_NullToken() {
        boolean valid = jwtUtil.isValid(null);

        assertFalse(valid);
    }
}
