package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.service.YeepayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "支付-易宝", description = "易宝支付下单与异步通知")
@RestController
@RequestMapping("/api/yeepay")
@RequiredArgsConstructor
public class YeepayController {

    private final YeepayService yeepayService;

    @Operation(summary = "创建易宝支付订单")
    @PostMapping("/pay")
    public ApiResponse<Map<String, Object>> createPayment(@RequestParam String dingDanHao) {
        Map<String, Object> result = yeepayService.createPayment(dingDanHao);
        return ApiResponse.success(result);
    }

    @PostMapping("/notify")
    public String handleNotify(@RequestParam Map<String, String> params) {
        if (yeepayService.verifyCallback(params)) {
            String dingDanHao = params.get("orderId");
            String transactionId = params.get("transactionId");

            yeepayService.handlePaymentCallback(dingDanHao, transactionId);

            return "success";
        }

        return "fail";
    }

    @GetMapping("/return")
    public String handleReturn(@RequestParam Map<String, String> params) {
        if (yeepayService.verifyCallback(params)) {
            String dingDanHao = params.get("orderId");
            return "redirect:/order/success?dingDanHao=" + dingDanHao;
        }

        return "redirect:/order/fail";
    }
}