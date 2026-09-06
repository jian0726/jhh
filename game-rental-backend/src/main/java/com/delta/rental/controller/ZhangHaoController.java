package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.DengLuRequest;
import com.delta.rental.dto.ZhuCeRequest;
import com.delta.rental.entity.YongHu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.CaptchaService;
import com.delta.rental.service.YongHuService;
import com.delta.rental.repository.YongHuRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ZhangHaoController {

  private final YongHuService yongHuService;
  private final CaptchaService captchaService;
  private final YongHuRepository yongHuRepository;
  private final PasswordEncoder passwordEncoder;

  @GetMapping("/fasong-yanzhengma")
  public ApiResponse<Map<String, Object>> fasongYanZhengMa(@RequestParam String shouJiHao) {
    String key = "sms:" + shouJiHao;
    String yanzhengma = captchaService.generateCaptcha(key);
    
    Map<String, Object> result = new HashMap<>();
    result.put("shouJiHao", shouJiHao);
    result.put("yanZhengMa", yanzhengma); // 实际生产环境不应返回验证码
    result.put("success", true);
    
    return ApiResponse.success(result);
  }

  @PostMapping("/send-email-code")
  public ApiResponse<Map<String, Object>> sendEmailCode(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    if (email == null || email.isBlank()) {
      return ApiResponse.error(400, "邮箱不能为空");
    }
    String key = "email:" + email;
    String code = captchaService.generateCaptcha(key);

    Map<String, Object> result = new HashMap<>();
    result.put("email", email);
    result.put("success", true);
    result.put("message", "验证码发送成功，请检查邮箱");
    return ApiResponse.success(result);
  }

  @PostMapping("/reset-password")
  public ApiResponse<Map<String, Object>> resetPassword(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String code = body.get("verifyCode");
    String newPassword = body.get("miMa");

    if (email == null || email.isBlank()) {
      return ApiResponse.error(400, "邮箱不能为空");
    }
    if (code == null || code.isBlank()) {
      return ApiResponse.error(400, "验证码不能为空");
    }
    if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
      return ApiResponse.error(400, "密码长度至少为6位");
    }

    String key = "email:" + email;
    if (!captchaService.validateCaptcha(key, code)) {
      return ApiResponse.error(400, "验证码错误或已过期");
    }

    YongHu yongHu = yongHuRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(404, "该邮箱未注册"));

    yongHu.setMiMa(passwordEncoder.encode(newPassword));
    yongHuRepository.save(yongHu);

    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("message", "密码重置成功");
    return ApiResponse.success(result);
  }

  @PostMapping("/zhuce")
  public ApiResponse<Map<String, Object>> zhuce(@Valid @RequestBody ZhuCeRequest request) {
    return ApiResponse.success(yongHuService.zhuCe(request));
  }

  @PostMapping("/denglu")
  public ApiResponse<Map<String, Object>> denglu(@Valid @RequestBody DengLuRequest request) {
    return ApiResponse.success(yongHuService.dengLu(request));
  }

  @GetMapping("/yonghu/{id}")
  public ApiResponse<YongHu> getYongHu(@PathVariable Long id) {
    return ApiResponse.success(yongHuService.getYongHuById(id));
  }

  @PostMapping("/shiming")
  public ApiResponse<YongHu> shimingRenZheng(
      @RequestHeader("Authorization") String token,
      @RequestParam String zhenShiXingMing,
      @RequestParam String shenFenZheng) {
    Long yongHuId = tiQuYongHuId(token);
    return ApiResponse.success(yongHuService.shiMingRenZheng(yongHuId, zhenShiXingMing, shenFenZheng));
  }

  @PutMapping("/yonghu")
  public ApiResponse<YongHu> updateYongHuInfo(
      @RequestHeader("Authorization") String token,
      @RequestParam(required = false) String yongHuMing,
      @RequestParam(required = false) String shouJiHao) {
    Long yongHuId = tiQuYongHuId(token);
    return ApiResponse.success(yongHuService.updateYongHuInfo(yongHuId, yongHuMing, shouJiHao));
  }

  @PutMapping("/yonghu/mima")
  public ApiResponse<Void> updateMiMa(
      @RequestHeader("Authorization") String token,
      @RequestParam String oldPassword,
      @RequestParam String newPassword) {
    Long yongHuId = tiQuYongHuId(token);
    yongHuService.updateMiMa(yongHuId, oldPassword, newPassword);
    return ApiResponse.success("密码修改成功", null);
  }

  @GetMapping("/yonghu/shiming")
  public ApiResponse<Map<String, Object>> getShiMingStatus(@RequestHeader("Authorization") String token) {
    Long yongHuId = tiQuYongHuId(token);
    return ApiResponse.success(yongHuService.getShiMingStatus(yongHuId));
  }

  private Long tiQuYongHuId(String token) {
    if (token.startsWith("Bearer ")) {
      token = token.substring(7);
    }
    String[] parts = token.split("\\.");
    String payload = parts[1];
    String decoded = new String(java.util.Base64.getUrlDecoder().decode(payload));
    return Long
        .parseLong(com.google.gson.JsonParser.parseString(decoded).getAsJsonObject().get("userId").getAsString());
  }
}
