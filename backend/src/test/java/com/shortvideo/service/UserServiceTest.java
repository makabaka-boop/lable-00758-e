package com.shortvideo.service;

import com.shortvideo.entity.User;
import com.shortvideo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_password");
        testUser.setNickname("测试用户");
        testUser.setIsAdmin(false);
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

        User result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encoded_password");
    }

    @Test
    void login_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encoded_password")).thenReturn(false);

        User result = userService.login("testuser", "wrongpass");

        assertNull(result);
        verify(passwordEncoder).matches("wrongpass", "encoded_password");
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        User result = userService.login("nonexistent", "password123");

        assertNull(result);
        verify(userRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_EmptyUsername() {
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());

        User result = userService.login("", "password123");

        assertNull(result);
    }

    @Test
    void login_NullUsername() {
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        User result = userService.login(null, "password123");

        assertNull(result);
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new_password");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setPassword("encoded_new_password");
        newUser.setNickname("newuser");
        newUser.setIsAdmin(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.register("newuser", "password123", null);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithNickname() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        User result = userService.register("newuser", "password123", "我的昵称");

        assertNotNull(result);
        assertEquals("我的昵称", result.getNickname());
    }

    @Test
    void register_DuplicateUsername() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        User result = userService.register("existinguser", "password123", "昵称");

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertFalse(result.isPresent());
    }
}
