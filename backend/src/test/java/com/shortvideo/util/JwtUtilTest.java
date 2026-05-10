package com.shortvideo.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        jwtUtil.init();
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtil.generateToken(1L, "admin", true);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void generateToken_NormalUser() {
        String token = jwtUtil.generateToken(2L, "user", false);
        assertNotNull(token);
    }

    @Test
    void parseToken_ValidToken() {
        String token = jwtUtil.generateToken(1L, "admin", true);
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("admin", claims.get("username"));
        assertEquals(true, claims.get("isAdmin"));
    }

    @Test
    void parseToken_InvalidToken() {
        Claims claims = jwtUtil.parseToken("invalid.token.here");
        assertNull(claims);
    }

    @Test
    void getUserId_ValidToken() {
        String token = jwtUtil.generateToken(1L, "admin", true);
        Long userId = jwtUtil.getUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    void getUserId_InvalidToken() {
        Long userId = jwtUtil.getUserId("invalid.token");
        assertNull(userId);
    }

    @Test
    void isValid_ValidToken() {
        String token = jwtUtil.generateToken(1L, "admin", true);
        assertTrue(jwtUtil.isValid(token));
    }

    @Test
    void isValid_InvalidToken() {
        assertFalse(jwtUtil.isValid("invalid.token"));
    }

    @Test
    void isValid_ExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        jwtUtil.init();
        String token = jwtUtil.generateToken(1L, "admin", true);

        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        jwtUtil.init();

        assertFalse(jwtUtil.isValid(token));
    }

    @Test
    void parseToken_NullToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.parseToken(null));
    }

    @Test
    void parseToken_EmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.parseToken(""));
    }

    @Test
    void isValid_NullToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.isValid(null));
    }

    @Test
    void isValid_EmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.isValid(""));
    }

    @Test
    void tokenContainsCorrectClaims() {
        String token = jwtUtil.generateToken(42L, "testuser", false);
        Claims claims = jwtUtil.parseToken(token);
        assertNotNull(claims);
        assertEquals("42", claims.getSubject());
        assertEquals("testuser", claims.get("username"));
        assertEquals(false, claims.get("isAdmin"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void differentUsersGenerateDifferentTokens() {
        String token1 = jwtUtil.generateToken(1L, "admin", true);
        String token2 = jwtUtil.generateToken(2L, "user", false);
        assertNotEquals(token1, token2);
    }
}
