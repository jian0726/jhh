package com.delta.rental.service;

import com.delta.rental.entity.FengKongRule;
import com.delta.rental.entity.FengXian;
import com.delta.rental.entity.YongHu;
import com.delta.rental.entity.DingDan;
import com.delta.rental.repository.*;
import com.delta.rental.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FengKongService {

    private final FengKongRuleRepository fengKongRuleRepository;
    private final FengXianRepository fengXianRepository;
    private final YongHuRepository yongHuRepository;
    private final DingDanRepository dingDanRepository;
    private final ShangPinRepository shangPinRepository;

    public static final String RULE_IP_FREQUENCY = "IP_FREQUENCY";
    public static final String RULE_DEVICE_FREQUENCY = "DEVICE_FREQUENCY";
    public static final String RULE_USER_FREQUENCY = "USER_FREQUENCY";
    public static final String RULE_HIGH_RISK_USER = "HIGH_RISK_USER";
    public static final String RULE_BANNED_USER = "BANNED_USER";
    public static final String RULE_LARGE_AMOUNT = "LARGE_AMOUNT";
    public static final String RULE_SHORT_TERM频繁 = "SHORT_TERM频繁";

    @Transactional
    public FengKongResult checkOrderRisk(Long userId, Long shangPinId, String ip, String deviceFingerprint, BigDecimal amount) {
        List<FengKongRule> enabledRules = fengKongRuleRepository.findByEnabled(true);
        if (enabledRules.isEmpty()) {
            initializeDefaultRules();
            enabledRules = fengKongRuleRepository.findByEnabled(true);
        }

        List<String> triggeredRules = new ArrayList<>();
        int totalScore = 0;

        for (FengKongRule rule : enabledRules) {
            boolean triggered = checkRule(rule, userId, shangPinId, ip, deviceFingerprint, amount);
            if (triggered) {
                triggeredRules.add(rule.getRuleName());
                totalScore += rule.getRiskScore();
                logRiskEvent(userId, rule.getRuleCode(), rule.getRuleName(), ip, deviceFingerprint);
            }
        }

        String riskLevel = calculateRiskLevel(totalScore);
        boolean pass = totalScore < 60;

        return new FengKongResult(pass, totalScore, riskLevel, triggeredRules);
    }

    private boolean checkRule(FengKongRule rule, Long userId, Long shangPinId, String ip, String deviceFingerprint, BigDecimal amount) {
        switch (rule.getRuleCode()) {
            case RULE_IP_FREQUENCY:
                return checkIpFrequency(ip, rule.getThresholdValue(), rule.getTimeWindow());
            case RULE_DEVICE_FREQUENCY:
                return checkDeviceFrequency(deviceFingerprint, rule.getThresholdValue(), rule.getTimeWindow());
            case RULE_USER_FREQUENCY:
                return checkUserFrequency(userId, rule.getThresholdValue(), rule.getTimeWindow());
            case RULE_HIGH_RISK_USER:
                return checkHighRiskUser(userId, rule.getThresholdValue());
            case RULE_BANNED_USER:
                return checkBannedUser(userId);
            case RULE_LARGE_AMOUNT:
                return checkLargeAmount(amount, rule.getThresholdValue());
            case RULE_SHORT_TERM频繁:
                return checkShortTerm频繁(userId, shangPinId, rule.getThresholdValue());
            default:
                return false;
        }
    }

    private boolean checkIpFrequency(String ip, int threshold, int timeWindow) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindow);
        List<DingDan> recentOrders = dingDanRepository.findByFuKuanShiJianAfter(startTime);
        long count = recentOrders.stream()
                .filter(d -> ip.equals(getClientIpFromOrder(d)))
                .count();
        return count >= threshold;
    }

    private boolean checkDeviceFrequency(String deviceFingerprint, int threshold, int timeWindow) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return false;
        }
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindow);
        List<DingDan> recentOrders = dingDanRepository.findByFuKuanShiJianAfter(startTime);
        long count = recentOrders.stream()
                .filter(d -> deviceFingerprint.equals(getDeviceFingerprintFromOrder(d)))
                .count();
        return count >= threshold;
    }

    private boolean checkUserFrequency(Long userId, int threshold, int timeWindow) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindow);
        List<DingDan> userOrders = dingDanRepository.findByMaiJiaId2AndFuKuanShiJianAfter(userId, startTime);
        return userOrders.size() >= threshold;
    }

    private boolean checkHighRiskUser(Long userId, int threshold) {
        List<FengXian> userRisks = fengXianRepository.findByYongHuId(userId);
        return userRisks.size() >= threshold;
    }

    private boolean checkBannedUser(Long userId) {
        Optional<YongHu> user = yongHuRepository.findById(userId);
        return user.isPresent() && user.get().getZhuangTai() == 1;
    }

    private boolean checkLargeAmount(BigDecimal amount, int threshold) {
        return amount.compareTo(BigDecimal.valueOf(threshold)) > 0;
    }

    private boolean checkShortTerm频繁(Long userId, Long shangPinId, int threshold) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        List<DingDan> recentOrders = dingDanRepository.findByMaiJiaId2AndShangPinIdAndFuKuanShiJianAfter(
                userId, shangPinId, startTime);
        return recentOrders.size() >= threshold;
    }

    private String getClientIpFromOrder(DingDan order) {
        return order.getIp();
    }

    private String getDeviceFingerprintFromOrder(DingDan order) {
        return order.getSheBeiShiWen();
    }

    private String calculateRiskLevel(int score) {
        if (score >= 80) {
            return "HIGH";
        } else if (score >= 60) {
            return "MEDIUM";
        } else if (score >= 30) {
            return "LOW";
        } else {
            return "NORMAL";
        }
    }

    private void logRiskEvent(Long userId, String ruleCode, String ruleName, String ip, String deviceFingerprint) {
        FengXian fengXian = new FengXian();
        fengXian.setYongHuId(userId);
        fengXian.setFengXianLeiXing(ruleCode);
        fengXian.setMiaoShu("触发风控规则: " + ruleName);
        fengXian.setIp(ip);
        fengXian.setSheBeiShiWen(deviceFingerprint);
        fengXianRepository.save(fengXian);
    }

    @Transactional
    public void initializeDefaultRules() {
        if (fengKongRuleRepository.count() > 0) {
            return;
        }

        List<FengKongRule> defaultRules = Arrays.asList(
                createRule(RULE_IP_FREQUENCY, "IP频率限制", "FREQUENCY", 20, 5, 60, "同一IP在60分钟内下单超过5次"),
                createRule(RULE_DEVICE_FREQUENCY, "设备频率限制", "FREQUENCY", 20, 3, 60, "同一设备在60分钟内下单超过3次"),
                createRule(RULE_USER_FREQUENCY, "用户下单频率", "FREQUENCY", 15, 10, 60, "用户在60分钟内下单超过10次"),
                createRule(RULE_HIGH_RISK_USER, "高风险用户", "BEHAVIOR", 30, 3, 0, "用户有3次以上风险记录"),
                createRule(RULE_BANNED_USER, "封禁用户", "BLACKLIST", 100, 0, 0, "用户已被封禁"),
                createRule(RULE_LARGE_AMOUNT, "大额交易", "AMOUNT", 25, 1000, 0, "单笔订单金额超过1000元"),
                createRule(RULE_SHORT_TERM频繁, "短时间频繁租用", "BEHAVIOR", 20, 2, 60, "1小时内对同一商品下单超过2次")
        );

        fengKongRuleRepository.saveAll(defaultRules);
    }

    private FengKongRule createRule(String code, String name, String type, int score, int threshold, int timeWindow, String desc) {
        FengKongRule rule = new FengKongRule();
        rule.setRuleCode(code);
        rule.setRuleName(name);
        rule.setRuleType(type);
        rule.setRiskScore(score);
        rule.setThresholdValue(threshold);
        rule.setTimeWindow(timeWindow);
        rule.setEnabled(true);
        rule.setDescription(desc);
        return rule;
    }

    public List<FengKongRule> getAllRules() {
        return fengKongRuleRepository.findAll();
    }

    public List<FengKongRule> getEnabledRules() {
        return fengKongRuleRepository.findByEnabled(true);
    }

    @Transactional
    public FengKongRule updateRule(Long ruleId, FengKongRule updatedRule) {
        FengKongRule rule = fengKongRuleRepository.findById(ruleId)
                .orElseThrow(() -> new BusinessException(404, "风控规则不存在"));
        
        rule.setRuleName(updatedRule.getRuleName());
        rule.setRiskScore(updatedRule.getRiskScore());
        rule.setThresholdValue(updatedRule.getThresholdValue());
        rule.setTimeWindow(updatedRule.getTimeWindow());
        rule.setEnabled(updatedRule.getEnabled());
        rule.setDescription(updatedRule.getDescription());
        
        return fengKongRuleRepository.save(rule);
    }

    public Map<String, Object> getRiskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRules", fengKongRuleRepository.count());
        stats.put("enabledRules", fengKongRuleRepository.findByEnabled(true).size());
        stats.put("totalRiskEvents", fengXianRepository.count());
        
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<FengXian> todayRisks = fengXianRepository.findAll().stream()
                .filter(f -> f.getChuangJianShiJian().isAfter(today))
                .toList();
        stats.put("todayRiskEvents", todayRisks.size());
        
        Map<String, Long> riskTypeStats = new HashMap<>();
        List<FengXian> allRisks = fengXianRepository.findAll();
        for (FengXian risk : allRisks) {
            riskTypeStats.merge(risk.getFengXianLeiXing(), 1L, Long::sum);
        }
        stats.put("riskTypeDistribution", riskTypeStats);
        
        return stats;
    }

    public static class FengKongResult {
        private final boolean pass;
        private final int totalScore;
        private final String riskLevel;
        private final List<String> triggeredRules;

        public FengKongResult(boolean pass, int totalScore, String riskLevel, List<String> triggeredRules) {
            this.pass = pass;
            this.totalScore = totalScore;
            this.riskLevel = riskLevel;
            this.triggeredRules = triggeredRules;
        }

        public boolean isPass() {
            return pass;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public List<String> getTriggeredRules() {
            return triggeredRules;
        }
    }
}
