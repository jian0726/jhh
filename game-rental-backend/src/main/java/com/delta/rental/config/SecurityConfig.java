package com.delta.rental.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 安全配置：无状态 JWT 认证 + 基于路由的 RBAC。
 *
 * <p>角色映射见 {@link JwtAuthenticationFilter}：0=ROLE_USER，1=ROLE_SELLER，2=ROLE_ADMIN。
 *
 * <p>放行策略的划分原则：
 * <ul>
 *   <li>认证/注册/找回密码、第三方登录回调、支付回调、验证码、公开浏览类 GET → 匿名可访问</li>
 *   <li>{@code /api/admin/**} 与商品审核、轮播图写操作、风控规则管理 → 仅 ROLE_ADMIN</li>
 *   <li>其余全部 → 必须登录</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 允许的跨域来源模式，逗号分隔；生产环境请改为具体域名。 */
    @Value("${app.cors.allowed-origin-patterns:*}")
    private String allowedOriginPatterns;

    /** 静态资源与接口文档。 */
    private static final String[] PUBLIC_DOCS = {
            "/error",
            "/favicon.ico",
            "/uploads/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    /** 匿名可访问的认证类接口（登录、注册、验证码、找回密码）。 */
    private static final String[] PUBLIC_AUTH = {
            "/api/auth/denglu",
            "/api/auth/zhuce",
            "/api/auth/fasong-yanzhengma",
            "/api/auth/send-email-code",
            "/api/auth/reset-password"
    };

    /** 第三方登录与支付回调（由外部平台调用，不能要求登录态）。 */
    private static final String[] PUBLIC_CALLBACK = {
            "/api/oauth/**",
            "/api/yeepay/notify",
            "/api/yeepay/return",
            "/api/wechat-score/notify"
    };

    /** 登录/注册前置的风控与验证码接口。 */
    private static final String[] PUBLIC_RISK = {
            "/api/captcha",
            "/api/captcha/verify",
            "/api/fingerprint",
            "/api/fengkong/check"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ---------- 静态资源与接口文档 ----------
                        .requestMatchers(PUBLIC_DOCS).permitAll()
                        // ---------- 认证、回调、风控前置 ----------
                        .requestMatchers(PUBLIC_AUTH).permitAll()
                        .requestMatchers(PUBLIC_CALLBACK).permitAll()
                        .requestMatchers(PUBLIC_RISK).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/yonghu/**").permitAll()
                        // ---------- 公开浏览（游客可看首页/商品/评价） ----------
                        .requestMatchers(HttpMethod.GET, "/api/lunbotu").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/shangpin/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/shangpin/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pingjia/shangpin/**").permitAll()
                        // ---------- 管理端：仅管理员 ----------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/lunbotu/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/lunbotu").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lunbotu/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lunbotu/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shangpin/**/tongguo").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shangpin/**/jujue").hasRole("ADMIN")
                        .requestMatchers("/api/fengkong/rules/**").hasRole("ADMIN")
                        .requestMatchers("/api/fengkong/statistics").hasRole("ADMIN")
                        .requestMatchers("/api/fengkong/events/**").hasRole("ADMIN")
                        // ---------- 其余：必须登录 ----------
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeJson(response, 401, "请先登录后再操作"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeJson(response, 403, "没有权限执行该操作"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 使用 patterns 以支持 allowCredentials=true
        List<String> patterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        configuration.setAllowedOriginPatterns(patterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 以统一的 {@code {code,message,data}} 信封返回鉴权失败，前端拦截器可直接取 message。
     */
    private void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":" + status + ",\"message\":\"" + message + "\","
                        + "\"data\":{\"success\":false,\"message\":\"" + message + "\"}}");
    }
}
