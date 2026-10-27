package com.delta.rental.security;

import com.delta.rental.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * 当前登录用户上下文。
 *
 * <p>{@code SecurityUtils} 是控制器获取「当前用户」的唯一入口，
 * 它依赖 {@code JwtAuthenticationFilter} 写入的已验签 principal。
 */
class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate(Long userId, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority(role))));
    }

    @Test
    @DisplayName("未登录时 getCurrentUserId 抛 401，getCurrentUserIdOrNull 返回 null")
    void notLoggedIn() {
        Throwable thrown = catchThrowable(SecurityUtils::getCurrentUserId);

        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) thrown).getCode()).isEqualTo(401);
        assertThat(SecurityUtils.getCurrentUserIdOrNull()).isNull();
    }

    @Test
    @DisplayName("匿名身份不被视为已登录")
    void anonymousIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "anonymousKey", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThat(SecurityUtils.getCurrentUserIdOrNull()).isNull();
        assertThat(SecurityUtils.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("已认证用户返回其 id，管理员判定为 true")
    void adminUser() {
        authenticate(7L, "ROLE_ADMIN");

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(7L);
        assertThat(SecurityUtils.isAdmin()).isTrue();
        assertThat(SecurityUtils.hasRole("ROLE_ADMIN")).isTrue();
    }

    @Test
    @DisplayName("普通用户 isAdmin 为 false，未登录时 hasRole 为 false")
    void normalUser() {
        authenticate(9L, "ROLE_USER");
        assertThat(SecurityUtils.isAdmin()).isFalse();

        SecurityContextHolder.clearContext();
        assertThat(SecurityUtils.hasRole("ROLE_USER")).isFalse();
    }
}
