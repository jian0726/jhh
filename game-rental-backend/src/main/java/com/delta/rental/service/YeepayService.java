package com.delta.rental.service;

import com.delta.rental.config.YeepayConfig;
import com.delta.rental.entity.DingDan;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.DingDanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class YeepayService {

    private final YeepayConfig yeepayConfig;
    private final DingDanRepository dingDanRepository;

    public Map<String, Object> createPayment(String dingDanHao) {
        DingDan order = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (order.getZhuangTai() != 0) {
            throw new BusinessException(400, "订单状态不正确");
        }

        Map<String, String> params = new HashMap<>();
        params.put("merchantId", yeepayConfig.getMerchantId());
        params.put("orderId", order.getDingDanHao());
        params.put("amount", order.getZongJinE().toString());
        params.put("currency", "CNY");
        params.put("productName", "游戏账号租赁");
        params.put("productDesc", "游戏账号租赁服务");
        params.put("notifyUrl", yeepayConfig.getNotifyUrl());
        params.put("returnUrl", yeepayConfig.getReturnUrl());
        params.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        params.put("version", yeepayConfig.getVersion());
        params.put("nonce", UUID.randomUUID().toString().replace("-", "").substring(0, 32));

        String sign = generateSign(params);
        params.put("sign", sign);

        Map<String, Object> result = new HashMap<>();
        result.put("gatewayUrl", yeepayConfig.getGatewayUrl() + "/pay");
        result.put("params", params);

        return result;
    }

    public boolean verifyCallback(Map<String, String> params) {
        String sign = params.remove("sign");
        String calculatedSign = generateSign(params);
        return sign.equals(calculatedSign);
    }

    public DingDan handlePaymentCallback(String dingDanHao, String transactionId) {
        DingDan order = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (order.getZhuangTai() != 0) {
            return order;
        }

        order.setZhuangTai(1);
        order.setFuKuanShiJian(java.time.LocalDateTime.now());
        order.setZuJieKaiShiShiJian(java.time.LocalDateTime.now());
        order.setZuJieJieShuShiJian(java.time.LocalDateTime.now().plusDays(order.getZuJieTianShu()));

        return dingDanRepository.save(order);
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
            sb.append("secretKey=").append(yeepayConfig.getSecretKey());

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
}