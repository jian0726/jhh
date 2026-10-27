package com.delta.rental.security;

import com.delta.rental.exception.BusinessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户上下文工具。
 *
 * <p>身份来源为 {@link com.delta.rental.config.JwtAuthenticationFilter} 校验 JWT 签名后写入
 * SecurityContext 的 {@code principal}（用户 id）。因此这里的 id 是<b>已验签</b>的，
 * 控制器不应再自行解析 Authorization 头。
 *
 * <p>历史写法：12 个控制器各自复制了一份 {@code tiQuYongHuId(token)}，只做 Base64 解码、
 * 不校验签名，可被伪造任意 userId。已全部改为调用本类。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户 id。
     *
     * @throws BusinessException 401 未登录 / 登录态失效
     */
    public static Long getCurrentUserId() {
        Long userId = getCurrentUserIdOrNull();
        if (userId == null) {
            throw new BusinessException(401, "请先登录后再操作");
        }
        return userId;
    }

    /**
     * 获取当前登录用户 id，未登录返回 {@code null}（用于「登录可选」的接口）。
     */
    public static Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /**
     * 当前登录用户是否为管理员（jueSe = 2）。
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * 当前登录用户是否具备指定角色，如 {@code ROLE_SELLER}。
     */
    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}
