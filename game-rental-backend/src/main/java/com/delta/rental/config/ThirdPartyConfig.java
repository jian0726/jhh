package com.delta.rental.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "third-party")
public class ThirdPartyConfig {

    private Wechat wechat = new Wechat();
    private QQ qq = new QQ();

    @Data
    public static class Wechat {
        private String appId;
        private String appSecret;
        private String redirectUri;
    }

    @Data
    public static class QQ {
        private String appId;
        private String appKey;
        private String redirectUri;
    }
}