package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.FengXian;
import com.delta.rental.entity.GuanLiYuan;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.entity.XiaoXi;
import com.delta.rental.entity.YongHu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.FengXianRepository;
import com.delta.rental.repository.GuanLiYuanRepository;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.security.SecurityUtils;
import com.delta.rental.service.GuanLiYuanService;
import com.delta.rental.service.ShangPinService;
import com.delta.rental.service.XiaoXiService;
import com.delta.rental.service.YongHuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台唯一入口（原 {@code GuanLiController} 已合并至此，避免同路径下存在两套接口）。
 *
 * <p>整段路径由 {@code SecurityConfig} 限制为 {@code ROLE_ADMIN}：未登录返回 401，
 * 非管理员返回 403。
 *
 * <p>为兼容既有调用方，部分接口保留两个等价路径：
 * <ul>
 *   <li>{@code GET /dashboard} 与 {@code GET /tongji}</li>
 *   <li>{@code GET /users} 与 {@code GET /yonghu}</li>
 *   <li>{@code GET /products} 与 {@code GET /shangpin/pending}</li>
 *   <li>{@code PUT /users/{id}/roles} 与 {@code PUT /yonghu/{id}/jueses}</li>
 * </ul>
 */
@Tag(name = "管理后台", description = "看板统计、用户管理、商品审核、订单与纠纷仲裁、风控与操作日志")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final YongHuRepository yongHuRepository;
    private final ShangPinRepository shangPinRepository;
    private final DingDanRepository dingDanRepository;
    private final FengXianRepository fengXianRepository;
    private final GuanLiYuanRepository guanLiYuanRepository;
    private final XiaoXiService xiaoXiService;
    private final YongHuService yongHuService;
    private final GuanLiYuanService guanLiYuanService;
    private final ShangPinService shangPinService;

    // ==================== 看板 / 统计 ====================

    @Operation(summary = "获取管理后台看板数据（/tongji 为等价别名）")
    @GetMapping({"/dashboard", "/tongji"})
    public ApiResponse<Map<String, Object>> getDashboard() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", yongHuRepository.count());
        dashboard.put("totalProducts", shangPinRepository.count());
        dashboard.put("totalOrders", dingDanRepository.count());
        dashboard.put("pendingProducts", shangPinRepository.findByZhuangTai(0).size());
        dashboard.put("pendingDisputes", dingDanRepository.findByZhuangTai(4).size());
        dashboard.put("todayOrders", dingDanRepository.findAll().stream()
                .filter(o -> o.getChuangJianShiJian() != null && o.getChuangJianShiJian().isAfter(todayStart))
                .count());

        List<Map<String, Object>> recentOrders = dingDanRepository.findAll().stream()
                .filter(o -> o.getChuangJianShiJian() != null)
                .sorted(Comparator.comparing(DingDan::getChuangJianShiJian).reversed())
                .limit(10)
                .map(order -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("dingDanHao", order.getDingDanHao());
                    item.put("zongJinE", order.getZongJinE());
                    item.put("zhuangTai", order.getZhuangTai());
                    item.put("chuangJianShiJian", order.getChuangJianShiJian());
                    return item;
                })
                .collect(Collectors.toList());
        dashboard.put("recentOrders", recentOrders);

        return ApiResponse.success(dashboard);
    }

    // ==================== 用户管理 ====================

    @Operation(summary = "获取全部用户（/yonghu 为等价别名；密码字段不外发）")
    @GetMapping({"/users", "/yonghu"})
    public ApiResponse<List<YongHu>> getAllUsers() {
        return ApiResponse.success(yongHuRepository.findAll());
    }

    @Operation(summary = "按状态封禁 / 解封用户（status：0 正常，1 封禁）")
    @PutMapping("/users/{id}/ban")
    public ApiResponse<YongHu> banUser(@PathVariable Long id, @RequestParam Integer status) {
        YongHu user = yongHuRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setZhuangTai(status);
        YongHu saved = yongHuRepository.save(user);
        log("BAN_YONGHU", "YongHu", id, "将用户状态置为 " + status);
        return ApiResponse.success(saved);
    }

    @Operation(summary = "封禁用户")
    @PutMapping("/yonghu/{id}/fengjin")
    public ApiResponse<YongHu> fengJinYongHu(@PathVariable Long id) {
        YongHu yongHu = yongHuService.fengJinYongHu(id);
        log("FENGJIN_YONGHU", "YongHu", id, "封禁用户");
        return ApiResponse.success(yongHu);
    }

    @Operation(summary = "解封用户")
    @PutMapping("/yonghu/{id}/jiefeng")
    public ApiResponse<YongHu> jieFengYongHu(@PathVariable Long id) {
        YongHu yongHu = yongHuService.jieFengYongHu(id);
        log("JIEFENG_YONGHU", "YongHu", id, "解封用户");
        return ApiResponse.success(yongHu);
    }

    @Operation(summary = "修改用户角色（jueSe：0 用户 / 1 卖家 / 2 管理员）")
    @PutMapping({"/users/{id}/roles", "/yonghu/{id}/jueses"})
    public ApiResponse<YongHu> updateYongHuJueSe(@PathVariable Long id, @RequestParam Integer jueSe) {
        YongHu yongHu = yongHuService.updateYongHuJueSe(id, jueSe);
        log("UPDATE_JUESE", "YongHu", id, "修改用户角色为 " + jueSe);
        return ApiResponse.success(yongHu);
    }

    // ==================== 商品审核 ====================

    @Operation(summary = "获取待审核商品（/shangpin/pending 为等价别名）")
    @GetMapping({"/products", "/shangpin/pending"})
    public ApiResponse<List<ShangPin>> getPendingProducts() {
        return ApiResponse.success(shangPinRepository.findByZhuangTai(0));
    }

    @Operation(summary = "获取全部商品（商品管理用，可按状态 / 标题关键词过滤；返回前端格式，已展开 metadata）")
    @GetMapping("/products/all")
    public ApiResponse<List<Map<String, Object>>> getAllProducts(
            @RequestParam(required = false) Integer zhuangTai,
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> result = shangPinRepository.findAll().stream()
                .filter(p -> zhuangTai == null || zhuangTai.equals(p.getZhuangTai()))
                .filter(p -> keyword == null || keyword.isBlank()
                        || (p.getBiaoTi() != null && p.getBiaoTi().contains(keyword.trim())))
                .sorted(Comparator.comparing(ShangPin::getId).reversed())
                .map(shangPinService::toFrontendFormat)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    @Operation(summary = "审核商品（result：1 通过，其它值视为拒绝）")
    @PutMapping("/products/{id}/audit")
    public ApiResponse<ShangPin> auditProduct(@PathVariable Long id, @RequestParam Integer result) {
        ShangPin product = shangPinRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        product.setZhuangTai(result != null && result == 1 ? 1 : 3);
        ShangPin saved = shangPinRepository.save(product);
        log("AUDIT_SHANGPIN", "ShangPin", id, "审核结果：" + (saved.getZhuangTai() == 1 ? "通过" : "拒绝"));
        return ApiResponse.success(saved);
    }

    @Operation(summary = "强制下架商品")
    @PutMapping("/products/{id}/offline")
    public ApiResponse<ShangPin> forceOffline(@PathVariable Long id) {
        ShangPin product = shangPinRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        product.setZhuangTai(3);
        ShangPin saved = shangPinRepository.save(product);
        log("OFFLINE_SHANGPIN", "ShangPin", id, "强制下架商品");
        return ApiResponse.success(saved);
    }

    // ==================== 订单与纠纷 ====================

    @Operation(summary = "获取全部订单")
    @GetMapping("/orders")
    public ApiResponse<List<DingDan>> getAllOrders() {
        return ApiResponse.success(dingDanRepository.findAll());
    }

    @Operation(summary = "获取纠纷中的订单列表")
    @GetMapping("/disputes")
    public ApiResponse<List<Map<String, Object>>> getAllDisputes() {
        List<Map<String, Object>> disputes = dingDanRepository.findByZhuangTai(4).stream()
                .map(order -> {
                    Map<String, Object> dispute = new HashMap<>();
                    dispute.put("id", order.getId());
                    dispute.put("dingDanHao", order.getDingDanHao());
                    dispute.put("shangPinId", order.getShangPinId());
                    dispute.put("maiJiaId", order.getMaiJiaId());
                    dispute.put("maiJiaId2", order.getMaiJiaId2());
                    dispute.put("zongJinE", order.getZongJinE());
                    dispute.put("chuangJianShiJian", order.getChuangJianShiJian());
                    return dispute;
                })
                .collect(Collectors.toList());

        return ApiResponse.success(disputes);
    }

    @Operation(summary = "仲裁订单纠纷（结束纠纷并把商品重新置为在售）")
    @PutMapping("/disputes/{id}/judge")
    public ApiResponse<DingDan> judgeDispute(@PathVariable Long id) {
        DingDan order = dingDanRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        order.setZhuangTai(2);

        ShangPin shangPin = shangPinRepository.findById(order.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);

        DingDan saved = dingDanRepository.save(order);
        log("JUDGE_JIUFENG", "DingDan", id, "仲裁纠纷，订单置为已完成");
        return ApiResponse.success(saved);
    }

    // ==================== 日志 ====================

    @Operation(summary = "查询风控风险事件日志")
    @GetMapping("/risk/logs")
    public ApiResponse<List<FengXian>> getRiskLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String riskType) {
        List<FengXian> logs;
        if (userId != null) {
            logs = fengXianRepository.findByYongHuId(userId);
        } else if (riskType != null) {
            logs = fengXianRepository.findByFengXianLeiXing(riskType);
        } else {
            logs = fengXianRepository.findAll();
        }
        return ApiResponse.success(logs);
    }

    @Operation(summary = "查询管理员操作日志（按时间倒序，最多 200 条）")
    @GetMapping("/operate/logs")
    public ApiResponse<List<Map<String, Object>>> getOperateLogs() {
        List<Map<String, Object>> result = guanLiYuanRepository.findAllByOrderByChuangJianShiJianDesc()
                .stream()
                .limit(200)
                .map(item -> {
                    Map<String, Object> vo = new HashMap<>();
                    vo.put("id", item.getId());
                    vo.put("guanLiYuanId", item.getGuanLiYuanId());
                    vo.put("operation", item.getCaoZuo());
                    vo.put("targetType", item.getMuBiaoLeiXing());
                    vo.put("targetId", item.getMuBiaoId());
                    vo.put("detail", item.getXiQing());
                    vo.put("time", item.getChuangJianShiJian());
                    return vo;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    // ==================== 通知 ====================

    @Operation(summary = "向全部用户发布站内通知")
    @PostMapping("/notice")
    public ApiResponse<XiaoXi> publishNotice(@RequestParam String title, @RequestParam String content) {
        List<YongHu> users = yongHuRepository.findAll();

        XiaoXi sample = null;
        for (YongHu user : users) {
            sample = xiaoXiService.createXiaoXi(user.getId(), title, content, 1);
        }

        log("PUBLISH_NOTICE", "XiaoXi", null, "发布通知：" + title);
        return ApiResponse.success(sample);
    }

    // ==================== 内部工具 ====================

    /** 记录当前管理员的本次操作；日志失败不影响主流程。 */
    private void log(String action, String targetType, Long targetId, String detail) {
        Long adminId = SecurityUtils.getCurrentUserIdOrNull();
        if (adminId == null) {
            return;
        }
        try {
            guanLiYuanService.logAction(adminId, action, targetType, targetId, detail);
        } catch (Exception ignored) {
            // 忽略日志写入异常
        }
    }
}
