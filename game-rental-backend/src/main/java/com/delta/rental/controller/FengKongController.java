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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "风控", description = "风险检查、风控规则维护与风险事件")
@RestController
@RequestMapping("/api/fengkong")
@RequiredArgsConstructor
public class FengKongController {

    private final FengKongService fengKongService;
    private final FengXianRepository fengXianRepository;

    @Operation(summary = "执行风控检查")
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

    @Operation(summary = "获取全部风控规则")
    @GetMapping("/rules")
    public ApiResponse<List<FengKongRule>> getAllRules() {
        return ApiResponse.success(fengKongService.getAllRules());
    }

    @Operation(summary = "获取启用中的风控规则")
    @GetMapping("/rules/enabled")
    public ApiResponse<List<FengKongRule>> getEnabledRules() {
        return ApiResponse.success(fengKongService.getEnabledRules());
    }

    @Operation(summary = "更新风控规则")
    @PutMapping("/rules/{id}")
    public ApiResponse<FengKongRule> updateRule(
            @PathVariable Long id,
            @RequestBody FengKongRule rule) {
        return ApiResponse.success(fengKongService.updateRule(id, rule));
    }

    @Operation(summary = "获取风控统计")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        return ApiResponse.success(fengKongService.getRiskStatistics());
    }

    @Operation(summary = "查询风险事件")
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

    @Operation(summary = "查询指定用户的风险事件")
    @GetMapping("/events/user/{userId}")
    public ApiResponse<List<FengXian>> getUserRiskEvents(@PathVariable Long userId) {
        return ApiResponse.success(fengXianRepository.findByYongHuId(userId));
    }

    @Operation(summary = "初始化默认风控规则")
    @PostMapping("/rules/init")
    public ApiResponse<Void> initializeRules() {
        fengKongService.initializeDefaultRules();
        return ApiResponse.success("风控规则初始化成功", null);
    }
}
