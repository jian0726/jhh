package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.QianBao;
import com.delta.rental.entity.TiXianShenQing;
import com.delta.rental.entity.TuiKuanShenQing;
import com.delta.rental.service.TiXianService;
import com.delta.rental.service.TuiKuanService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "退款与钱包", description = "退款申请与仲裁、提现申请、钱包余额")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FinanceController {

    private final TuiKuanService tuiKuanService;
    private final TiXianService tiXianService;

    // ========== 退款相关接口 ==========

    @Operation(summary = "申请退款")
    @PostMapping("/orders/{dingDanHao}/refund")
    public ApiResponse<TuiKuanShenQing> applyRefund(
            @PathVariable String dingDanHao,
            @RequestParam String reason,
            @RequestParam(required = false) String evidenceUrl) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        TuiKuanShenQing result = tuiKuanService.applyRefund(userId, dingDanHao, reason, evidenceUrl);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询退款进度")
    @GetMapping("/orders/{dingDanHao}/refund-status")
    public ApiResponse<TuiKuanShenQing> getRefundStatus(@PathVariable String dingDanHao) {
        TuiKuanShenQing result = tuiKuanService.getRefundStatus(dingDanHao);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取我的退款申请列表")
    @GetMapping("/refunds")
    public ApiResponse<Page<TuiKuanShenQing>> getRefundList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<TuiKuanShenQing> result = tuiKuanService.getRefundList(userId, pageable);
        return ApiResponse.success(result);
    }

    @Operation(summary = "卖家处理退款")
    @PutMapping("/orders/{dingDanHao}/refund/seller")
    public ApiResponse<String> sellerHandleRefund(
            @PathVariable String dingDanHao,
            @RequestParam boolean agree) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        tuiKuanService.sellerHandleRefund(userId, dingDanHao, agree);
        return ApiResponse.success(agree ? "同意退款成功" : "拒绝退款成功");
    }

    @Operation(summary = "管理员仲裁退款")
    @PutMapping("/admin/orders/{dingDanHao}/refund")
    public ApiResponse<String> adminHandleRefund(
            @PathVariable String dingDanHao,
            @RequestParam boolean agree,
            @RequestParam(required = false) String remark) {
        
        Long adminId = SecurityUtils.getCurrentUserId();
        tuiKuanService.adminHandleRefund(adminId, dingDanHao, agree, remark);
        return ApiResponse.success(agree ? "退款成功" : "拒绝退款成功");
    }

    // ========== 提现相关接口 ==========

    @Operation(summary = "申请提现")
    @PostMapping("/withdraw")
    public ApiResponse<TiXianShenQing> applyWithdraw(
            @RequestBody Map<String, Object> request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String method = (String) request.get("method");
        String account = (String) request.get("account");
        String name = (String) request.get("name");

        TiXianShenQing result = tiXianService.applyWithdraw(userId, amount, method, account, name);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取我的提现记录")
    @GetMapping("/withdraw/list")
    public ApiResponse<Page<TiXianShenQing>> getWithdrawList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<TiXianShenQing> result = tiXianService.getWithdrawList(userId, pageable);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询钱包余额")
    @GetMapping("/wallet/balance")
    public ApiResponse<QianBao> getBalance() {
        Long userId = SecurityUtils.getCurrentUserId();
        QianBao result = tiXianService.getBalance(userId);
        return ApiResponse.success(result);
    }

}