package com.delta.rental.config;

import com.delta.rental.controller.AdminController;
import com.delta.rental.controller.LunBoTuController;
import com.delta.rental.controller.XiaoXiController;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.FengXianRepository;
import com.delta.rental.repository.GuanLiYuanRepository;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.service.FileStorageService;
import com.delta.rental.service.GuanLiYuanService;
import com.delta.rental.service.LunBoTuService;
import com.delta.rental.service.XiaoXiService;
import com.delta.rental.service.YongHuService;
import com.delta.rental.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 访问控制矩阵（不依赖数据库：服务层全部 mock）。
 *
 * <p>覆盖三类角色 × 三类路径：
 * <ul>
 *   <li>匿名 → 公开接口 200 / 受保护接口 401</li>
 *   <li>普通用户 → 受保护接口 200 / 管理接口 403</li>
 *   <li>管理员 → 管理接口 200</li>
 * </ul>
 * 另外验证「伪造签名的令牌」会被拒绝（历史实现可借此提权）。
 */
@WebMvcTest(controllers = {XiaoXiController.class, LunBoTuController.class, AdminController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtUtil.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private XiaoXiService xiaoXiService;
    @MockBean
    private LunBoTuService lunBoTuService;
    @MockBean
    private YongHuService yongHuService;
    @MockBean
    private GuanLiYuanService guanLiYuanService;
    @MockBean
    private YongHuRepository yongHuRepository;
    @MockBean
    private ShangPinRepository shangPinRepository;
    @MockBean
    private DingDanRepository dingDanRepository;
    @MockBean
    private FengXianRepository fengXianRepository;
    @MockBean
    private GuanLiYuanRepository guanLiYuanRepository;
    /** WebConfig（WebMvcConfigurer）在 @WebMvcTest 下会被自动装配，需要这个依赖。 */
    @MockBean
    private FileStorageService fileStorageService;

    private String tokenOf(long userId, int role) {
        return "Bearer " + jwtUtil.generateToken(userId, "test-user", role);
    }

    private static String forgedToken() {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS384\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"userId\":1,\"username\":\"admin\",\"role\":2,\"sub\":\"admin\"}"
                        .getBytes(StandardCharsets.UTF_8));
        return "Bearer " + header + "." + payload + ".forgedsignature";
    }

    // ==================== 匿名 ====================

    @Test
    @DisplayName("匿名可以访问公开接口（轮播图）")
    void anonymousCanReadPublicEndpoint() throws Exception {
        when(lunBoTuService.getActiveLunBoTu()).thenReturn(List.of());

        mockMvc.perform(get("/api/lunbotu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("匿名访问受保护接口返回 401，且响应体是统一信封")
    void anonymousGetsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/xiaoxi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先登录后再操作"))
                .andExpect(jsonPath("$.data.success").value(false));
    }

    @Test
    @DisplayName("匿名访问管理接口返回 401")
    void anonymousGetsUnauthorizedOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("伪造签名的令牌无效：即便 payload 里写着 userId=1 / role=2 也是 401")
    void forgedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/xiaoxi").header("Authorization", forgedToken()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users").header("Authorization", forgedToken()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 普通用户 ====================

    @Test
    @DisplayName("普通用户可以访问自己的受保护接口")
    void userCanAccessOwnEndpoint() throws Exception {
        when(xiaoXiService.getYongHuXiaoXi(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/xiaoxi").header("Authorization", tokenOf(5L, 0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("普通用户访问管理接口返回 403（不是 401）")
    void userGetsForbiddenOnAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users").header("Authorization", tokenOf(5L, 0)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("没有权限执行该操作"));
    }

    // ==================== 管理员 ====================

    @Test
    @DisplayName("管理员可以访问管理接口，且用户列表不返回密码字段")
    void adminCanAccessAdminEndpoint() throws Exception {
        when(yongHuRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").header("Authorization", tokenOf(1L, 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("管理接口的等价别名同样需要管理员（/api/admin/yonghu、/api/admin/tongji）")
    void adminAliasPathsAreProtected() throws Exception {
        mockMvc.perform(get("/api/admin/yonghu").header("Authorization", tokenOf(5L, 0)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/tongji"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 无效令牌 ====================

    @Test
    @DisplayName("格式错误的令牌按未登录处理")
    void malformedTokenIsTreatedAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/xiaoxi").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/xiaoxi").header("Authorization", "Basic abcdefg"))
                .andExpect(status().isUnauthorized());
    }
}
