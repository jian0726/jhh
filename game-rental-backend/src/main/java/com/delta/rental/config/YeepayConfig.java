package com.delta.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "yeepay")
public class YeepayConfig {

    private String merchantId;
    private String secretKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl = "https://api.yeepay.com";
    private String version = "1.0";
}