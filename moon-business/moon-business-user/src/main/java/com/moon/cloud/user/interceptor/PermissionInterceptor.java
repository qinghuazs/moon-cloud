package com.moon.cloud.user.interceptor;

import com.moon.cloud.user.annotation.RequirePermission;
import com.moon.cloud.user.annotation.RequireRole;
import com.moon.cloud.user.service.AuthService;
import com.moon.cloud.user.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 * 用于处理权限注解和角色注解
 *
 * @author Moon Cloud
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();

        // 检查方法级别的权限注解
        RequirePermission methodPermission = method.getAnnotation(RequirePermission.class);
        RequireRole methodRole = method.getAnnotation(RequireRole.class);

        // 检查类级别的权限注解
        RequirePermission classPermission = clazz.getAnnotation(RequirePermission.class);
        RequireRole classRole = clazz.getAnnotation(RequireRole.class);

        // 方法级别注解优先于类级别注解
        RequirePermission permission = methodPermission != null ? methodPermission : classPermission;
        RequireRole role = methodRole != null ? methodRole : classRole;

        // 如果没有权限注解和角色注解，直接放行
        if (permission == null && role == null) {
            return true;
        }

        // 获取JWT令牌
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("访问受保护资源但未提供JWT令牌: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未认证\",\"data\":null}");
            return false;
        }

        // 验证令牌
        if (!authService.validateToken(token)) {
            log.warn("访问受保护资源但JWT令牌无效: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效\",\"data\":null}");
            return false;
        }

        // 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无法从JWT令牌中获取用户ID: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌无效\",\"data\":null}");
            return false;
        }

        // 检查权限
        if (permission != null) {
            if (!checkPermission(userId, permission)) {
                log.warn("用户[{}]访问受保护资源但权限不足: {}", userId, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                return false;
            }
        }

        // 检查角色
        if (role != null) {
            if (!checkRole(userId, role)) {
                log.warn("用户[{}]访问受保护资源但角色不足: {}", userId, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"角色不足\",\"data\":null}");
                return false;
            }
        }

        return true;
    }

    /**
     * 从请求中提取JWT令牌
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(Long userId, RequirePermission permission) {
        // 获取权限编码列表
        List<String> permissionCodes = getPermissionCodes(permission);
        if (permissionCodes.isEmpty()) {
            return true;
        }

        // 根据逻辑关系检查权限
        if (permission.logical() == RequirePermission.Logical.AND) {
            // AND关系：需要拥有所有权限
            for (String permissionCode : permissionCodes) {
                if (!authService.hasPermission(userId, permissionCode)) {
                    return false;
                }
            }
            return true;
        } else {
            // OR关系：拥有其中一个权限即可
            for (String permissionCode : permissionCodes) {
                if (authService.hasPermission(userId, permissionCode)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 检查角色
     */
    private boolean checkRole(Long userId, RequireRole role) {
        // 获取角色编码列表
        List<String> roleCodes = getRoleCodes(role);
        if (roleCodes.isEmpty()) {
            return true;
        }

        // 根据逻辑关系检查角色
        if (role.logical() == RequireRole.Logical.AND) {
            // AND关系：需要拥有所有角色
            for (String roleCode : roleCodes) {
                if (!authService.hasRole(userId, roleCode)) {
                    return false;
                }
            }
            return true;
        } else {
            // OR关系：拥有其中一个角色即可
            for (String roleCode : roleCodes) {
                if (authService.hasRole(userId, roleCode)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 获取权限编码列表
     */
    private List<String> getPermissionCodes(RequirePermission permission) {
        List<String> codes = Arrays.asList(permission.value());
        if (codes.isEmpty() || (codes.size() == 1 && codes.get(0).isEmpty())) {
            codes = Arrays.asList(permission.permissions());
        }
        return codes.stream().filter(StringUtils::hasText).toList();
    }

    /**
     * 获取角色编码列表
     */
    private List<String> getRoleCodes(RequireRole role) {
        List<String> codes = Arrays.asList(role.value());
        if (codes.isEmpty() || (codes.size() == 1 && codes.get(0).isEmpty())) {
            codes = Arrays.asList(role.roles());
        }
        return codes.stream().filter(StringUtils::hasText).toList();
    }
}