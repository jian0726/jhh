package com.delta.rental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ZhuCeRequest {
    // 手机注册模式必填
    private String yongHuMing;

    @NotBlank(message = "密码不能为空")
    private String miMa;

    // 手机注册模式必填
    private String shouJiHao;

    // 邮箱注册模式必填
    private String email;

    // 邮箱注册/忘记密码时必填的验证码
    private String verifyCode;
}
