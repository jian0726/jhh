package com.delta.rental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DengLuRequest {
    @NotBlank(message = "用户名不能为空")
    private String yongHuMing;
    
    @NotBlank(message = "密码不能为空")
    private String miMa;
}
