package com.delta.rental.controller;

import com.delta.rental.dto.ApiResponse;
import com.delta.rental.dto.DengLuRequest;
import com.delta.rental.dto.ZhuCeRequest;
import com.delta.rental.entity.YongHu;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.service.CaptchaService;
import com.delta.rental.service.YongHuService;
import com.delta.rental.repository.YongHuRepository;
import com.delta.rental.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "账号与认证", description = "注册、登录、验证码、找回密码、实名认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ZhangHaoController {

  private final YongHuService yongHuService;
  private final CaptchaService captchaService;
  private final YongHuRepository yongHuRepository;
  private final PasswordEncoder passwordEncoder;

    @Operation(summary = "发送短信验证码")
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

    @Operation(summary = "发送邮箱验证码")
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

    @Operation(summary = "通过邮箱验证码重置密码")
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

    @Operation(summary = "用户注册")
  @PostMapping("/zhuce")
  public ApiResponse<Map<String, Object>> zhuce(@Valid @RequestBody ZhuCeRequest request) {
    return ApiResponse.success(yongHuService.zhuCe(request));
  }

    @Operation(summary = "用户登录")
  @PostMapping("/denglu")
  public ApiResponse<Map<String, Object>> denglu(@Valid @RequestBody DengLuRequest request) {
    return ApiResponse.success(yongHuService.dengLu(request));
  }

    @Operation(summary = "查询用户公开信息")
  @GetMapping("/yonghu/{id}")
  public ApiResponse<Map<String, Object>> getYongHu(@PathVariable Long id) {
    // 返回脱敏后的用户信息，避免把密码等敏感字段透出给前端
    return ApiResponse.success(yongHuService.toUserVo(yongHuService.getYongHuById(id)));
  }

    @Operation(summary = "提交实名认证")
  @PostMapping("/shiming")
  public ApiResponse<YongHu> shimingRenZheng(
      @RequestParam String zhenShiXingMing,
      @RequestParam String shenFenZheng) {
    Long yongHuId = SecurityUtils.getCurrentUserId();
    return ApiResponse.success(yongHuService.shiMingRenZheng(yongHuId, zhenShiXingMing, shenFenZheng));
  }

    @Operation(summary = "修改用户名 / 手机号")
  @PutMapping("/yonghu")
  public ApiResponse<YongHu> updateYongHuInfo(
      @RequestParam(required = false) String yongHuMing,
      @RequestParam(required = false) String shouJiHao) {
    Long yongHuId = SecurityUtils.getCurrentUserId();
    return ApiResponse.success(yongHuService.updateYongHuInfo(yongHuId, yongHuMing, shouJiHao));
  }

    @Operation(summary = "修改登录密码")
  @PutMapping("/yonghu/mima")
  public ApiResponse<Void> updateMiMa(
      @RequestParam String oldPassword,
      @RequestParam String newPassword) {
    Long yongHuId = SecurityUtils.getCurrentUserId();
    yongHuService.updateMiMa(yongHuId, oldPassword, newPassword);
    return ApiResponse.success("密码修改成功", null);
  }

    @Operation(summary = "查询实名认证状态")
  @GetMapping("/yonghu/shiming")
  public ApiResponse<Map<String, Object>> getShiMingStatus() {
    Long yongHuId = SecurityUtils.getCurrentUserId();
    return ApiResponse.success(yongHuService.getShiMingStatus(yongHuId));
  }

}
