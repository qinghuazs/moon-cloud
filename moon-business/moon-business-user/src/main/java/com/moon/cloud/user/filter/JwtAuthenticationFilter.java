package com.moon.cloud.user.filter;

import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.util.JwtUtil;
import com.moon.cloud.user.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AuthService authService;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_NAME = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 获取JWT令牌
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // 验证令牌格式和有效性
                if (jwtUtil.validateToken(jwt)) {
                    // 检查令牌是否在黑名单中
                    String jti = jwtUtil.getJtiFromToken(jwt);
                    if (jti != null && redisUtil.isTokenBlacklisted(jti)) {
                        logger.warn("Token is blacklisted: {}", jti);
                    } else {
                        // 从令牌中获取用户信息
                        String username = jwtUtil.getUsernameFromToken(jwt);
                        Long userId = jwtUtil.getUserIdFromToken(jwt);
                        
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            // 加载用户详情
                            UserDetails userDetails = ((com.moon.cloud.user.service.impl.AuthServiceImpl) authService).loadUserByUsernameForSecurity(username);
                            
                            // 验证令牌与用户信息是否匹配
                            if (jwtUtil.validateToken(jwt, username)) {
                                // 创建认证对象
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                
                                // 设置到安全上下文
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                // 将用户ID添加到请求属性中，方便后续使用
                                request.setAttribute("userId", userId);
                                request.setAttribute("username", username);
                                
                                logger.debug("Successfully authenticated user: {}", username);
                            } else {
                                logger.warn("JWT token validation failed for user: {}", username);
                            }
                        }
                    }
                } else {
                    logger.warn("Invalid JWT token");
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 判断是否跳过过滤器
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 跳过公开接口
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/api/auth/logout") ||
               path.startsWith("/api/auth/captcha") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/error") ||
               path.startsWith("/actuator");
    }
}