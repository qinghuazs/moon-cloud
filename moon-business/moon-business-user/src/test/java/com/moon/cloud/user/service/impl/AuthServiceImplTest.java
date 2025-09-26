package com.moon.cloud.user.service.impl;

import com.moon.cloud.user.dto.LoginResponse;
import com.moon.cloud.user.dto.RegisterRequest;
import com.moon.cloud.user.entity.User;
import com.moon.cloud.user.exception.AuthException;
import com.moon.cloud.user.mapper.UserMapper;
import com.moon.cloud.user.service.LoginLogService;
import com.moon.cloud.user.util.JwtUtil;
import com.moon.cloud.user.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl 单元测试类
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LoginLogService loginLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("Test User");
        registerRequest.setPhone("13800138000");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setStatus(User.STATUS_ENABLED);
    }

    @Test
    void register_Success_ShouldReturnLoginResponse() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(registerRequest.getPhone())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(invocation -> null).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        LoginResponse response = authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());

        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper).selectByEmail(registerRequest.getEmail());
        verify(userMapper).selectByPhone(registerRequest.getPhone());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userMapper).insert(any(User.class));
        verify(userMapper).assignDefaultRole(anyLong());
        verify(loginLogService).recordLoginLog(anyLong(), eq("127.0.0.1"), eq("Mozilla/5.0"), eq(1));
        verify(redisUtil).cacheUserInfo(anyLong(), any(User.class), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void register_UsernameExists_ShouldThrowAuthException() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(existingUser);

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0")
        );

        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper, never()).selectByEmail(anyString());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void register_EmailExists_ShouldThrowAuthException() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(existingUser);

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0")
        );

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper).selectByEmail(registerRequest.getEmail());
        verify(userMapper, never()).selectByPhone(anyString());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void register_PhoneExists_ShouldThrowAuthException() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(registerRequest.getPhone())).thenReturn(existingUser);

        // When & Then
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0")
        );

        assertEquals("手机号已被注册", exception.getMessage());
        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper).selectByEmail(registerRequest.getEmail());
        verify(userMapper).selectByPhone(registerRequest.getPhone());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void register_NullPhone_ShouldSkipPhoneCheck() {
        // Given
        registerRequest.setPhone(null);
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(invocation -> null).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        LoginResponse response = authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertNotNull(response);
        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper).selectByEmail(registerRequest.getEmail());
        verify(userMapper, never()).selectByPhone(anyString());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_EmptyPhone_ShouldSkipPhoneCheck() {
        // Given
        registerRequest.setPhone("  ");
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(invocation -> null).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        LoginResponse response = authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertNotNull(response);
        verify(userMapper).selectByUsername(registerRequest.getUsername());
        verify(userMapper).selectByEmail(registerRequest.getEmail());
        verify(userMapper, never()).selectByPhone(anyString());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_DefaultRoleAssignmentFails_ShouldContinueRegistration() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(registerRequest.getPhone())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return null;
        }).when(userMapper).insert(any(User.class));

        doThrow(new RuntimeException("Role assignment failed")).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        LoginResponse response = authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());

        verify(userMapper).insert(any(User.class));
        verify(userMapper).assignDefaultRole(anyLong());
        verify(loginLogService).recordLoginLog(anyLong(), eq("127.0.0.1"), eq("Mozilla/5.0"), eq(1));
        verify(redisUtil).cacheUserInfo(anyLong(), any(User.class), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    void register_NullNickname_ShouldUseUsernameAsNickname() {
        // Given
        registerRequest.setNickname(null);
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(registerRequest.getPhone())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            assertEquals(registerRequest.getUsername(), user.getNickname());
            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(invocation -> null).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        LoginResponse response = authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        assertNotNull(response);
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_ShouldSetCorrectUserProperties() {
        // Given
        when(userMapper.selectByUsername(registerRequest.getUsername())).thenReturn(null);
        when(userMapper.selectByEmail(registerRequest.getEmail())).thenReturn(null);
        when(userMapper.selectByPhone(registerRequest.getPhone())).thenReturn(null);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(jwtUtil.generateTokenWithJti(anyLong(), anyString(), anyString())).thenReturn("access_token");
        when(jwtUtil.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh_token");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);

            // Verify user properties
            assertEquals(registerRequest.getUsername(), user.getUsername());
            assertEquals(registerRequest.getEmail(), user.getEmail());
            assertEquals("encodedPassword", user.getPasswordHash());
            assertEquals(registerRequest.getNickname(), user.getNickname());
            assertEquals(registerRequest.getPhone(), user.getPhone());
            assertEquals(User.PROVIDER_LOCAL, user.getProviderType());
            assertEquals(false, user.getIsEmailVerified());
            assertEquals(User.STATUS_ENABLED, user.getStatus());
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());

            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(invocation -> null).when(userMapper).assignDefaultRole(anyLong());
        doAnswer(invocation -> null).when(loginLogService).recordLoginLog(anyLong(), anyString(), anyString(), anyInt());
        doAnswer(invocation -> null).when(redisUtil).cacheUserInfo(anyLong(), any(User.class), anyLong(), any(TimeUnit.class));

        // When
        authService.register(registerRequest, "127.0.0.1", "Mozilla/5.0");

        // Then
        verify(userMapper).insert(any(User.class));
    }
}