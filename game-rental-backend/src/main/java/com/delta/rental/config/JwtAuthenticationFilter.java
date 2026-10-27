package com.delta.rental.config;

import com.delta.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器：解析并<b>验签</b> Authorization 头，把用户 id 作为 principal 写入
 * SecurityContext，同时按 jueSe 赋予 ROLE_USER / ROLE_SELLER / ROLE_ADMIN 权限。
 *
 * <p>令牌非法、被篡改或过期时保持未认证状态，由 {@link SecurityConfig} 的路由规则决定是否放行。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            try {
                JwtUtil.JwtPayload payload = jwtUtil.parsePayload(token);
                if (payload != null && payload.userId() != null && !jwtUtil.isExpired(payload)) {
                    String roleName = switch (payload.role() == null ? 0 : payload.role()) {
                        case 1 -> "ROLE_SELLER";
                        case 2 -> "ROLE_ADMIN";
                        default -> "ROLE_USER";
                    };

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    payload.userId(),
                                    null,
                                    List.of(new SimpleGrantedAuthority(roleName))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // 令牌非法/被篡改/过期：不设置认证信息，交由路由规则返回 401/403
                log.debug("JWT 校验失败：{}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
