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

  /**
   * 清理已过期的验证码，避免内存中的缓存无限增长。
   *
   * <p>由 {@code ScheduledTasks#cleanExpiredCaptcha} 定期调用。
   *
   * @return 被清理的条数
   */
  public int cleanUpExpired() {
    int before = captchaCache.size();
    captchaCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    return before - captchaCache.size();
  }

  /** 当前缓存中的验证码条数（便于监控与测试）。 */
  public int size() {
    return captchaCache.size();
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