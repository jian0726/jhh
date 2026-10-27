package com.delta.rental.service;

import com.delta.rental.dto.DengLuRequest;
import com.delta.rental.dto.ZhuCeRequest;
import com.delta.rental.entity.YongHu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录 / 注册核心链路。
 *
 * <p>用真实的 {@link BCryptPasswordEncoder} 与真实的密码比对逻辑，
 * 只把仓库、JWT 工具、验证码服务替换为 mock，保证「密码校验」这段逻辑是真跑的。
 */
@ExtendWith(MockitoExtension.class)
class YongHuServiceTest {

    @Mock
    private YongHuRepository yongHuRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private CaptchaService captchaService;

    private PasswordEncoder passwordEncoder;
    private YongHuService yongHuService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        yongHuService = new YongHuService(yongHuRepository, passwordEncoder, jwtUtil, captchaService);
    }

    private YongHu persistedUser(String rawPassword) {
        YongHu user = new YongHu();
        user.setId(1L);
        user.setYongHuMing("admin");
        user.setMiMa(passwordEncoder.encode(rawPassword));
        user.setJueSe(2);
        user.setZhuangTai(0);
        return user;
    }

    private static DengLuRequest loginRequest(String username, String password) {
        DengLuRequest request = new DengLuRequest();
        request.setYongHuMing(username);
        request.setMiMa(password);
        return request;
    }

    @Test
    @DisplayName("登录成功：返回 token 与脱敏后的用户信息，不包含密码")
    void loginSuccess() {
        when(yongHuRepository.findByYongHuMing("admin")).thenReturn(Optional.of(persistedUser("admin123")));
        when(jwtUtil.generateToken(anyLong(), anyString(), any())).thenReturn("mocked-jwt-token");

        Map<String, Object> result = yongHuService.dengLu(loginRequest("admin", "admin123"));

        assertThat(result.get("token")).isEqualTo("mocked-jwt-token");
        assertThat(result.get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> vo = (Map<String, Object>) result.get("user");
        assertThat(vo).containsEntry("yongHuMing", "admin").containsEntry("jueSe", 2);
        assertThat(vo).doesNotContainKey("miMa");
        assertThat(result.toString()).doesNotContain("$2a$");
    }

    @Test
    @DisplayName("密码错误：抛 400，且不签发令牌")
    void loginWithWrongPassword() {
        when(yongHuRepository.findByYongHuMing("admin")).thenReturn(Optional.of(persistedUser("admin123")));

        Throwable thrown = catchThrowable(() -> yongHuService.dengLu(loginRequest("admin", "wrong-password")));

        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) thrown).getCode()).isEqualTo(400);
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("用户不存在：抛 400，不泄露「用户是否存在」")
    void loginWithUnknownUser() {
        when(yongHuRepository.findByYongHuMing(anyString())).thenReturn(Optional.empty());
        when(yongHuRepository.findByShouJiHao(anyString())).thenReturn(Optional.empty());
        when(yongHuRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> yongHuService.dengLu(loginRequest("nobody", "whatever")));

        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(thrown).hasMessage("用户名或密码错误");
    }

    @Test
    @DisplayName("账号被封禁：抛 403")
    void loginWithBannedUser() {
        YongHu banned = persistedUser("admin123");
        banned.setZhuangTai(1);
        when(yongHuRepository.findByYongHuMing("admin")).thenReturn(Optional.of(banned));

        Throwable thrown = catchThrowable(() -> yongHuService.dengLu(loginRequest("admin", "admin123")));

        assertThat(thrown).isInstanceOf(BusinessException.class);
        assertThat(((BusinessException) thrown).getCode()).isEqualTo(403);
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("手机号登录：用户名查不到时回退到手机号")
    void loginByPhoneNumber() {
        when(yongHuRepository.findByYongHuMing("13800138000")).thenReturn(Optional.empty());
        when(yongHuRepository.findByShouJiHao("13800138000")).thenReturn(Optional.of(persistedUser("admin123")));
        when(jwtUtil.generateToken(anyLong(), anyString(), any())).thenReturn("token-by-phone");

        Map<String, Object> result = yongHuService.dengLu(loginRequest("13800138000", "admin123"));

        assertThat(result.get("token")).isEqualTo("token-by-phone");
    }

    @Test
    @DisplayName("注册：密码以 BCrypt 存储，不是明文")
    void registerHashesPassword() {
        when(yongHuRepository.existsByYongHuMing("newbie")).thenReturn(false);
        when(yongHuRepository.existsByShouJiHao("13900000002")).thenReturn(false);
        when(yongHuRepository.save(any(YongHu.class))).thenAnswer(invocation -> {
            YongHu saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });
        when(jwtUtil.generateToken(anyLong(), anyString(), any())).thenReturn("new-token");

        ZhuCeRequest request = new ZhuCeRequest();
        request.setYongHuMing("newbie");
        request.setShouJiHao("13900000002");
        request.setMiMa("plain-password");

        Map<String, Object> result = yongHuService.zhuCe(request);

        assertThat(result.get("token")).isEqualTo("new-token");

        org.mockito.ArgumentCaptor<YongHu> captor = org.mockito.ArgumentCaptor.forClass(YongHu.class);
        verify(yongHuRepository).save(captor.capture());
        assertThat(captor.getValue().getMiMa()).isNotEqualTo("plain-password");
        assertThat(captor.getValue().getMiMa()).startsWith("$2");
        assertThat(passwordEncoder.matches("plain-password", captor.getValue().getMiMa())).isTrue();
    }

    @Test
    @DisplayName("实名认证：写入姓名与身份证并把状态置为已实名")
    void shiMingRenZheng() {
        YongHu user = persistedUser("admin123");
        when(yongHuRepository.findById(1L)).thenReturn(Optional.of(user));
        when(yongHuRepository.save(any(YongHu.class))).thenAnswer(inv -> inv.getArgument(0));

        YongHu saved = yongHuService.shiMingRenZheng(1L, "张三", "110101199001011234");

        assertThat(saved.getShiFouShiMing()).isEqualTo(1);
        assertThat(saved.getZhenShiXingMing()).isEqualTo("张三");
    }
}
