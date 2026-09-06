package com.delta.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.score")
public class WechatScoreConfig {

    private String appId;
    private String mchId;
    private String apiKey;
    private String appSecret;
    private String notifyUrl;
    private String serviceId;
}