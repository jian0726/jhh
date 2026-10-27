package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.DingDanRequest;
import com.delta.rental.entity.DingDan;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.DingDanService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "订单", description = "下单、付款、验号、收货、归还与凭证获取")
@RestController
@RequestMapping("/api/dingdan")
@RequiredArgsConstructor
public class DingDanController {

    private final DingDanService dingDanService;

    @Operation(summary = "买家下单（创建订单）")
    @PostMapping
    public ApiResponse<DingDan> danJiaDingDan(
            @RequestBody DingDanRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        Long maiJiaId2 = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.createDingDan(maiJiaId2, request, ip, deviceFingerprint));
    }

    @Operation(summary = "按订单号查询订单详情")
    @GetMapping("/{dingDanHao}")
    public ApiResponse<DingDan> getDingDan(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.getDingDanByHao(dingDanHao));
    }

    @Operation(summary = "订单付款")
    @PostMapping("/{dingDanHao}/fukuan")
    public ApiResponse<DingDan> fuKuanDingDan(
            @PathVariable String dingDanHao,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        return ApiResponse.success(dingDanService.fuKuanDingDan(dingDanHao, ip, deviceFingerprint));
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{dingDanHao}/wancheng")
    public ApiResponse<DingDan> wanChengDingDan(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.wanChengDingDan(dingDanHao));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{dingDanHao}/quxiao")
    public ApiResponse<DingDan> quXiaoDingDan(
            @PathVariable String dingDanHao) {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.quXiaoDingDan(dingDanHao, yongHuId));
    }

    @Operation(summary = "发起纠纷申诉")
    @PostMapping("/{dingDanHao}/jiufeng")
    public ApiResponse<DingDan> jiuFengDingDan(
            @PathVariable String dingDanHao,
            @RequestParam String yuanYin) {
        Long yongHuId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.jiufengDingDan(dingDanHao, yongHuId, yuanYin));
    }

    @Operation(summary = "处理纠纷申诉")
    @PostMapping("/{dingDanHao}/chuli")
    public ApiResponse<DingDan> chuLiJiuFeng(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.chuliJiufeng(dingDanHao));
    }

    @Operation(summary = "获取我作为卖家的订单")
    @GetMapping("/maijia")
    public ApiResponse<List<DingDan>> getMaiJiaDingDan() {
        Long maiJiaId2 = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.getMaiJiaDingDan(maiJiaId2));
    }

    @Operation(summary = "按卖家 ID 查询其订单")
    @GetMapping("/maimai/{maiJiaId}")
    public ApiResponse<List<DingDan>> getMaiJiaDingDanById(@PathVariable Long maiJiaId) {
        return ApiResponse.success(dingDanService.getMaiJiaDingDanById(maiJiaId));
    }

    @Operation(summary = "按商品查询订单")
    @GetMapping("/shangpin/{shangPinId}")
    public ApiResponse<List<DingDan>> getShangPinDingDan(@PathVariable Long shangPinId) {
        return ApiResponse.success(dingDanService.getShangPinDingDan(shangPinId));
    }

    @Operation(summary = "获取我的订单列表")
    @GetMapping
    public ApiResponse<List<DingDan>> getMyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.getMyOrders(userId));
    }

    @Operation(summary = "获取订单临时登录凭证")
    @GetMapping("/{dingDanHao}/credential")
    public ApiResponse<java.util.Map<String, Object>> getCredential(
            @PathVariable String dingDanHao) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.getCredential(dingDanHao, userId));
    }

    @Operation(summary = "归还账号（结束租期）")
    @PutMapping("/{dingDanHao}/return")
    public ApiResponse<DingDan> returnOrder(
            @PathVariable String dingDanHao) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.returnOrder(dingDanHao, userId));
    }

    @Operation(summary = "查询租期剩余时间")
    @GetMapping("/{dingDanHao}/remaining")
    public ApiResponse<java.util.Map<String, Object>> getRemainingTime(@PathVariable String dingDanHao) {
        return ApiResponse.success(dingDanService.getRemainingTime(dingDanHao));
    }

    @Operation(summary = "发起验号")
    @PostMapping("/{dingDanHao}/inspect-start")
    public ApiResponse<DingDan> inspectStart(
            @PathVariable String dingDanHao) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.inspectStart(dingDanHao, userId));
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{dingDanHao}/confirm")
    public ApiResponse<DingDan> confirmOrder(
            @PathVariable String dingDanHao) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.confirmOrder(dingDanHao, userId));
    }

    @Operation(summary = "拒绝收货（触发纠纷）")
    @PutMapping("/{dingDanHao}/reject")
    public ApiResponse<DingDan> rejectOrder(
            @PathVariable String dingDanHao,
            @RequestParam String reason) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(dingDanService.rejectOrder(dingDanHao, userId, reason));
    }

}
