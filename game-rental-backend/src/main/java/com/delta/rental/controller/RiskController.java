package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.FengXian;
import com.delta.rental.repository.FengXianRepository;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.service.CaptchaService;
import com.delta.rental.service.FengKongService;
import com.delta.rental.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "风控-指纹与验证码", description = "设备指纹上报、风险状态、图形验证码")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RiskController {

    private final FengXianRepository fengXianRepository;
    private final YongHuRepository yongHuRepository;
    private final FengKongService fengKongService;
    private final CaptchaService captchaService;

    @Operation(summary = "上报设备指纹")
    @PostMapping("/fingerprint")
    public ApiResponse<Map<String, Object>> uploadFingerprint(
            @RequestParam String fingerprint) {

        Long userId = SecurityUtils.getCurrentUserId();

        FengXian fengXian = new FengXian();
        fengXian.setYongHuId(userId);
        fengXian.setFengXianLeiXing("FINGERPRINT");
        fengXian.setMiaoShu("设备指纹上报: " + fingerprint);
        fengXian.setSheBeiShiWen(fingerprint);
        fengXianRepository.save(fengXian);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "设备指纹上报成功");

        return ApiResponse.success(result);
    }

    @Operation(summary = "查询当前用户风险状态")
    @GetMapping("/risk/status")
    public ApiResponse<Map<String, Object>> getRiskStatus() {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> result = new HashMap<>();

        var user = yongHuRepository.findById(userId);
        if (user.isEmpty()) {
            result.put("status", "UNKNOWN");
            result.put("message", "用户不存在");
            return ApiResponse.success(result);
        }

        if (user.get().getZhuangTai() == 1) {
            result.put("status", "BANNED");
            result.put("message", "账号已被封禁");
            return ApiResponse.success(result);
        }

        List<FengXian> risks = fengXianRepository.findByYongHuId(userId);
        int riskCount = risks.size();

        if (riskCount >= 5) {
            result.put("status", "HIGH_RISK");
            result.put("message", "高风险用户");
            result.put("riskCount", riskCount);
        } else if (riskCount >= 3) {
            result.put("status", "MEDIUM_RISK");
            result.put("message", "中风险用户");
            result.put("riskCount", riskCount);
        } else {
            result.put("status", "NORMAL");
            result.put("message", "正常");
            result.put("riskCount", riskCount);
        }

        return ApiResponse.success(result);
    }

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public ApiResponse<Map<String, Object>> getCaptcha(@RequestParam String key) {
        String captcha = captchaService.generateCaptcha(key);

        Map<String, Object> result = new HashMap<>();
        result.put("captcha", captcha);

        return ApiResponse.success(result);
    }

    @Operation(summary = "校验图形验证码")
    @PostMapping("/captcha/verify")
    public ApiResponse<Boolean> verifyCaptcha(
            @RequestParam String key,
            @RequestParam String captcha) {

        boolean isValid = captchaService.validateCaptcha(key, captcha);
        return ApiResponse.success(isValid);
    }

}
