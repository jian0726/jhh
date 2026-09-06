package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.QianBao;
import com.delta.rental.entity.TiXianShenQing;
import com.delta.rental.entity.TuiKuanShenQing;
import com.delta.rental.service.TiXianService;
import com.delta.rental.service.TuiKuanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FinanceController {

    private final TuiKuanService tuiKuanService;
    private final TiXianService tiXianService;

    // ========== 退款相关接口 ==========

    @PostMapping("/orders/{dingDanHao}/refund")
    public ApiResponse<TuiKuanShenQing> applyRefund(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token,
            @RequestParam String reason,
            @RequestParam(required = false) String evidenceUrl) {
        
        Long userId = tiQuYongHuId(token);
        TuiKuanShenQing result = tuiKuanService.applyRefund(userId, dingDanHao, reason, evidenceUrl);
        return ApiResponse.success(result);
    }

    @GetMapping("/orders/{dingDanHao}/refund-status")
    public ApiResponse<TuiKuanShenQing> getRefundStatus(@PathVariable String dingDanHao) {
        TuiKuanShenQing result = tuiKuanService.getRefundStatus(dingDanHao);
        return ApiResponse.success(result);
    }

    @GetMapping("/refunds")
    public ApiResponse<Page<TuiKuanShenQing>> getRefundList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<TuiKuanShenQing> result = tuiKuanService.getRefundList(userId, pageable);
        return ApiResponse.success(result);
    }

    @PutMapping("/orders/{dingDanHao}/refund/seller")
    public ApiResponse<String> sellerHandleRefund(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token,
            @RequestParam boolean agree) {
        
        Long userId = tiQuYongHuId(token);
        tuiKuanService.sellerHandleRefund(userId, dingDanHao, agree);
        return ApiResponse.success(agree ? "同意退款成功" : "拒绝退款成功");
    }

    @PutMapping("/admin/orders/{dingDanHao}/refund")
    public ApiResponse<String> adminHandleRefund(
            @PathVariable String dingDanHao,
            @RequestHeader("Authorization") String token,
            @RequestParam boolean agree,
            @RequestParam(required = false) String remark) {
        
        Long adminId = tiQuYongHuId(token);
        tuiKuanService.adminHandleRefund(adminId, dingDanHao, agree, remark);
        return ApiResponse.success(agree ? "退款成功" : "拒绝退款成功");
    }

    // ========== 提现相关接口 ==========

    @PostMapping("/withdraw")
    public ApiResponse<TiXianShenQing> applyWithdraw(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        
        Long userId = tiQuYongHuId(token);
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String method = (String) request.get("method");
        String account = (String) request.get("account");
        String name = (String) request.get("name");

        TiXianShenQing result = tiXianService.applyWithdraw(userId, amount, method, account, name);
        return ApiResponse.success(result);
    }

    @GetMapping("/withdraw/list")
    public ApiResponse<Page<TiXianShenQing>> getWithdrawList(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = tiQuYongHuId(token);
        Pageable pageable = PageRequest.of(page, size);
        Page<TiXianShenQing> result = tiXianService.getWithdrawList(userId, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/wallet/balance")
    public ApiResponse<QianBao> getBalance(@RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);
        QianBao result = tiXianService.getBalance(userId);
        return ApiResponse.success(result);
    }

    private Long tiQuYongHuId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String[] parts = token.split("\\.");
        String payload = parts[1];
        String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
        return Long.parseLong(
                com.google.gson.JsonParser.parseString(decoded).getAsJsonObject().get("userId").getAsString());
    }
}