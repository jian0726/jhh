package com.delta.rental.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT 签发与验签。
 *
 * <p>重点覆盖「篡改令牌必须被拒绝」——历史实现只做 Base64 解码、不验签，
 * 导致可伪造任意 userId 提权。
 */
class JwtUtilTest {

    private static final String SECRET = "DeltaRentalPlatformSecretKey2024ForJWTTokenGeneration";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = jwtUtilWith(SECRET, 3600_000L);
    }

    private static JwtUtil jwtUtilWith(String secret, long expiration) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", secret);
        ReflectionTestUtils.setField(util, "expiration", expiration);
        return util;
    }

    @Test
    @DisplayName("签发后解析出的载荷与入参一致")
    void roundTrip() {
        String token = jwtUtil.generateToken(42L, "tester", 1);

        JwtUtil.JwtPayload payload = jwtUtil.parsePayload(token);

        assertThat(payload.userId()).isEqualTo(42L);
        assertThat(payload.username()).isEqualTo("tester");
        assertThat(payload.role()).isEqualTo(1);
        assertThat(jwtUtil.isExpired(payload)).isFalse();
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("伪造签名的令牌无法通过验签")
    void forgedSignatureRejected() {
        String token = jwtUtil.generateToken(1L, "admin", 2);
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1] + ".forgedsignature";

        assertThatThrownBy(() -> jwtUtil.parsePayload(forged)).isInstanceOf(JwtException.class);
        assertThat(jwtUtil.isTokenExpired(forged)).isTrue();
    }

    @Test
    @DisplayName("篡改 payload 使 userId 变为 1 也会被验签拦下")
    void tamperedPayloadRejected() {
        String token = jwtUtil.generateToken(999L, "someone", 0);
        String[] parts = token.split("\\.");

        String tamperedPayload = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"userId\":1,\"username\":\"admin\",\"role\":2,\"sub\":\"admin\"}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThatThrownBy(() -> jwtUtil.parsePayload(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("用其它密钥签发的令牌无法通过验签")
    void foreignKeyRejected() {
        JwtUtil other = jwtUtilWith("AnotherSecretKeyThatIsLongEnoughForHS384Algorithm!", 3600_000L);
        String foreignToken = other.generateToken(1L, "admin", 2);

        assertThatThrownBy(() -> jwtUtil.parsePayload(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("已过期令牌被判定为过期")
    void expiredToken() {
        JwtUtil expiredUtil = jwtUtilWith(SECRET, -1000L);
        String token = expiredUtil.generateToken(1L, "admin", 0);

        assertThat(expiredUtil.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("非 JWT 字符串被当作已过期处理，不抛异常")
    void malformedTokenTreatedAsExpired() {
        assertThat(jwtUtil.isTokenExpired("not-a-jwt")).isTrue();
        assertThat(jwtUtil.isTokenExpired("")).isTrue();
    }
}
