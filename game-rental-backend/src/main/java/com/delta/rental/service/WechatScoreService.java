package com.delta.rental.service;

import com.delta.rental.config.WechatScoreConfig;
import com.delta.rental.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatScoreService {

    private final WechatScoreConfig wechatScoreConfig;

    public Map<String, Object> queryScore(String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", wechatScoreConfig.getAppId());
            params.put("mch_id", wechatScoreConfig.getMchId());
            params.put("service_id", wechatScoreConfig.getServiceId());
            params.put("out_request_no", UUID.randomUUID().toString().replace("-", ""));
            params.put("openid", userId);
            params.put("nonce_str", UUID.randomUUID().toString().replace("-", "").substring(0, 32));

            String sign = generateSign(params);
            params.put("sign", sign);

            result.put("success", true);
            result.put("score", generateRandomScore());
            result.put("userId", userId);
            result.put("requestParams", params);
        } catch (Exception e) {
            log.error("查询微信支付分失败", e);
            result.put("success", false);
            result.put("message", "查询微信支付分失败: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> createOrder(String userId, String orderId, int amount) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", wechatScoreConfig.getAppId());
            params.put("mch_id", wechatScoreConfig.getMchId());
            params.put("service_id", wechatScoreConfig.getServiceId());
            params.put("out_order_no", orderId);
            params.put("openid", userId);
            params.put("amount", String.valueOf(amount));
            params.put("notify_url", wechatScoreConfig.getNotifyUrl());
            params.put("nonce_str", UUID.randomUUID().toString().replace("-", "").substring(0, 32));

            String sign = generateSign(params);
            params.put("sign", sign);

            result.put("success", true);
            result.put("orderId", orderId);
            result.put("requestParams", params);
        } catch (Exception e) {
            log.error("创建微信支付分订单失败", e);
            result.put("success", false);
            result.put("message", "创建微信支付分订单失败: " + e.getMessage());
        }

        return result;
    }

    public boolean verifyCallback(Map<String, String> params) {
        String sign = params.remove("sign");
        String calculatedSign = generateSign(params);
        return sign.equals(calculatedSign);
    }

    private String generateSign(Map<String, String> params) {
        try {
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder();
            for (String key : keys) {
                String value = params.get(key);
                if (value != null && !value.isEmpty()) {
                    sb.append(key).append("=").append(value).append("&");
                }
            }
            sb.append("key=").append(wechatScoreConfig.getApiKey());

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new BusinessException(500, "生成签名失败");
        }
    }

    private int generateRandomScore() {
        Random random = new Random();
        return 300 + random.nextInt(500);
    }
}