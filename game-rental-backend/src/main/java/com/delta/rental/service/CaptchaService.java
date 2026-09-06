package com.delta.rental.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CaptchaService {

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  private static final int CAPTCHA_EXPIRE_SECONDS = 300; // 5分钟

  private final Map<String, CaptchaInfo> captchaCache = new ConcurrentHashMap<>();

  private static class CaptchaInfo {
    String captcha;
    long expireTime;

    CaptchaInfo(String captcha) {
      this.captcha = captcha;
      this.expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_SECONDS * 1000L;
    }

    boolean isExpired() {
      return System.currentTimeMillis() > expireTime;
    }
  }

  public String generateCaptcha(String key) {
    String captcha = generateRandomCaptcha(6);
    captchaCache.put(key, new CaptchaInfo(captcha));
    log.info("生成验证码: key={}, captcha={}", key, captcha);
    return captcha;
  }

  public boolean validateCaptcha(String key, String captcha) {
    CaptchaInfo info = captchaCache.remove(key);

    if (info == null || info.isExpired()) {
      return false;
    }

    return info.captcha.equalsIgnoreCase(captcha);
  }

  private String generateRandomCaptcha(int length) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}