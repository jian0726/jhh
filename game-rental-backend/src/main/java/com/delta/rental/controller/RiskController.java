package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.entity.FengXian;
import com.delta.rental.repository.FengXianRepository;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.service.CaptchaService;
import com.delta.rental.service.FengKongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RiskController {

    private final FengXianRepository fengXianRepository;
    private final YongHuRepository yongHuRepository;
    private final FengKongService fengKongService;
    private final CaptchaService captchaService;

    @PostMapping("/fingerprint")
    public ApiResponse<Map<String, Object>> uploadFingerprint(
            @RequestHeader("Authorization") String token,
            @RequestParam String fingerprint) {

        Long userId = tiQuYongHuId(token);

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

    @GetMapping("/risk/status")
    public ApiResponse<Map<String, Object>> getRiskStatus(@RequestHeader("Authorization") String token) {
        Long userId = tiQuYongHuId(token);

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

    @GetMapping("/captcha")
    public ApiResponse<Map<String, Object>> getCaptcha(@RequestParam String key) {
        String captcha = captchaService.generateCaptcha(key);

        Map<String, Object> result = new HashMap<>();
        result.put("captcha", captcha);

        return ApiResponse.success(result);
    }

    @PostMapping("/captcha/verify")
    public ApiResponse<Boolean> verifyCaptcha(
            @RequestParam String key,
            @RequestParam String captcha) {

        boolean isValid = captchaService.validateCaptcha(key, captcha);
        return ApiResponse.success(isValid);
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
