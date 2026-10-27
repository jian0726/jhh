package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.DingDan;
import com.delta.rental.service.DingDanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "支付", description = "订单支付与支付状态查询")
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final DingDanService dingDanService;

    @Operation(summary = "订单支付")
    @PostMapping
    public ApiResponse<Map<String, Object>> payOrder(
            @RequestParam String dingDanHao,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint) {
        
        DingDan dingDan = dingDanService.fuKuanDingDan(dingDanHao, ip, deviceFingerprint);
        
        Map<String, Object> result = new HashMap<>();
        result.put("dingDanHao", dingDan.getDingDanHao());
        result.put("status", "SUCCESS");
        result.put("message", "支付成功");
        
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{dingDanHao}")
    public ApiResponse<Map<String, Object>> getPayStatus(@PathVariable String dingDanHao) {
        DingDan dingDan = dingDanService.getDingDanByHao(dingDanHao);
        
        Map<String, Object> result = new HashMap<>();
        result.put("dingDanHao", dingDan.getDingDanHao());
        
        String status;
        String message;
        switch (dingDan.getZhuangTai()) {
            case 0:
                status = "PENDING";
                message = "待支付";
                break;
            case 1:
                status = "PAID";
                message = "已支付";
                break;
            case 2:
                status = "COMPLETED";
                message = "已完成";
                break;
            case 3:
                status = "CANCELLED";
                message = "已取消";
                break;
            case 4:
                status = "DISPUTE";
                message = "纠纷中";
                break;
            default:
                status = "UNKNOWN";
                message = "未知状态";
        }
        
        result.put("status", status);
        result.put("message", message);
        
        return ApiResponse.success(result);
    }
}
