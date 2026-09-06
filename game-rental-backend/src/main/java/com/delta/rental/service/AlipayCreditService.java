package com.delta.rental.service;

import com.delta.rental.config.AlipayCreditConfig;
import com.delta.rental.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayCreditService {

    private final AlipayCreditConfig alipayCreditConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> queryCreditScore(String userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String url = String.format(
                    "%s?app_id=%s&method=zhima.credit.score.get&charset=%s&sign_type=%s",
                    alipayCreditConfig.getGatewayUrl(),
                    alipayCreditConfig.getAppId(),
                    alipayCreditConfig.getCharset(),
                    alipayCreditConfig.getSignType()
            );

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("zhima_credit_score_get_response")) {
                Map<String, Object> responseData = (Map<String, Object>) body.get("zhima_credit_score_get_response");
                result.put("score", responseData.get("score"));
                result.put("userId", userId);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("message", "查询芝麻信用分失败");
            }
        } catch (Exception e) {
            log.error("查询芝麻信用分失败", e);
            result.put("success", false);
            result.put("message", "查询芝麻信用分失败: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> authorize(String userId, String authCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("app_id", alipayCreditConfig.getAppId());
            params.put("method", "zhima.credit.auth.order.create");
            params.put("charset", alipayCreditConfig.getCharset());
            params.put("sign_type", alipayCreditConfig.getSignType());
            params.put("auth_code", authCode);
            params.put("user_id", userId);

            String url = alipayCreditConfig.getGatewayUrl() + "?" + buildQueryString(params);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("zhima_credit_auth_order_create_response")) {
                result.put("success", true);
                result.put("message", "授权成功");
            } else {
                result.put("success", false);
                result.put("message", "授权失败");
            }
        } catch (Exception e) {
            log.error("芝麻信用授权失败", e);
            result.put("success", false);
            result.put("message", "授权失败: " + e.getMessage());
        }

        return result;
    }

    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}