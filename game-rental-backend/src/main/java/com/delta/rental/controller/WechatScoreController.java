package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.service.WechatScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "第三方信用-微信支付分", description = "支付分查询与订单创建")
@RestController
@RequestMapping("/api/wechat-score")
@RequiredArgsConstructor
public class WechatScoreController {

    private final WechatScoreService wechatScoreService;

    @Operation(summary = "查询微信支付分")
    @GetMapping("/score")
    public ApiResponse<Map<String, Object>> getScore(
            @RequestParam(required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            userId = "default_user";
        }
        
        Map<String, Object> result = wechatScoreService.queryScore(userId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "创建微信支付分订单")
    @PostMapping("/order")
    public ApiResponse<Map<String, Object>> createOrder(
            @RequestParam String orderId,
            @RequestParam int amount) {
        
        String userId = "default_user";
        Map<String, Object> result = wechatScoreService.createOrder(userId, orderId, amount);
        return ApiResponse.success(result);
    }

    @PostMapping("/notify")
    public String handleNotify(@RequestBody Map<String, String> params) {
        if (wechatScoreService.verifyCallback(params)) {
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        }

        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名失败]]></return_msg></xml>";
    }
}