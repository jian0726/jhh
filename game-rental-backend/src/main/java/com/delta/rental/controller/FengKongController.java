package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.FengKongRule;
import com.delta.rental.entity.FengXian;
import com.delta.rental.service.FengKongService;
import com.delta.rental.repository.FengXianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fengkong")
@RequiredArgsConstructor
public class FengKongController {

    private final FengKongService fengKongService;
    private final FengXianRepository fengXianRepository;

    @PostMapping("/check")
    public ApiResponse<FengKongService.FengKongResult> checkRisk(
            @RequestParam Long userId,
            @RequestParam Long shangPinId,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String deviceFingerprint,
            @RequestParam BigDecimal amount) {
        FengKongService.FengKongResult result = fengKongService.checkOrderRisk(
                userId, shangPinId, ip, deviceFingerprint, amount);
        return ApiResponse.success(result);
    }

    @GetMapping("/rules")
    public ApiResponse<List<FengKongRule>> getAllRules() {
        return ApiResponse.success(fengKongService.getAllRules());
    }

    @GetMapping("/rules/enabled")
    public ApiResponse<List<FengKongRule>> getEnabledRules() {
        return ApiResponse.success(fengKongService.getEnabledRules());
    }

    @PutMapping("/rules/{id}")
    public ApiResponse<FengKongRule> updateRule(
            @PathVariable Long id,
            @RequestBody FengKongRule rule) {
        return ApiResponse.success(fengKongService.updateRule(id, rule));
    }

    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        return ApiResponse.success(fengKongService.getRiskStatistics());
    }

    @GetMapping("/events")
    public ApiResponse<List<FengXian>> getRiskEvents(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String riskType) {
        List<FengXian> events;
        if (userId != null) {
            events = fengXianRepository.findByYongHuId(userId);
        } else if (riskType != null) {
            events = fengXianRepository.findByFengXianLeiXing(riskType);
        } else {
            events = fengXianRepository.findAll();
        }
        return ApiResponse.success(events);
    }

    @GetMapping("/events/user/{userId}")
    public ApiResponse<List<FengXian>> getUserRiskEvents(@PathVariable Long userId) {
        return ApiResponse.success(fengXianRepository.findByYongHuId(userId));
    }

    @PostMapping("/rules/init")
    public ApiResponse<Void> initializeRules() {
        fengKongService.initializeDefaultRules();
        return ApiResponse.success("风控规则初始化成功", null);
    }
}
