package com.delta.rental.config;

import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.TiXianShenQingRepository;
import com.delta.rental.repository.TuiKuanShenQingRepository;
import com.delta.rental.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单与资金相关的定时任务。
 *
 * <p>补上租赁平台状态机的自动闭环：
 * <ol>
 *   <li><b>关闭超时未支付订单</b>：下单后超过 {@code unpaid-order-timeout-minutes} 分钟仍未付款 → 置为已取消。
 *       订单创建时不动商品状态，因此关单也不需要回滚商品。</li>
 *   <li><b>租期到期自动完成</b>：租用中且 {@code zuJieJieShuShiJian} 已过 → 置为已完成，并把商品恢复为在售
 *       （与「归还账号 / 完成订单」的处理保持一致）。</li>
 *   <li><b>清理过期验证码</b>：{@link CaptchaService} 的验证码存在进程内存里，定期清理避免堆积。</li>
 *   <li><b>待办提醒</b>：长期挂起的提现 / 退款申请只做日志提醒，<b>不自动改状态</b>——涉及资金必须人工处理。</li>
 * </ol>
 *
 * <p>所有任务都可通过 {@code app.scheduled.*} 配置项调整或关闭；
 * 整体开关为 {@code app.scheduled.enabled}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledTasks {

    private final DingDanRepository dingDanRepository;
    private final ShangPinRepository shangPinRepository;
    private final TiXianShenQingRepository tiXianShenQingRepository;
    private final TuiKuanShenQingRepository tuiKuanShenQingRepository;
    private final CaptchaService captchaService;

    /** 未支付订单的超时阈值（分钟）。 */
    @Value("${app.scheduled.unpaid-order-timeout-minutes:30}")
    private int unpaidOrderTimeoutMinutes;

    /** 提现 / 退款挂起多久后开始告警（天）。 */
    @Value("${app.scheduled.stale-pending-days:3}")
    private int stalePendingDays;

    // ==================== 1. 关闭超时未支付订单 ====================

    @Transactional
    @Scheduled(
            initialDelayString = "${app.scheduled.initial-delay-ms:120000}",
            fixedDelayString = "${app.scheduled.close-unpaid-interval-ms:300000}")
    public void closeUnpaidOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(unpaidOrderTimeoutMinutes);
        List<DingDan> unpaid = dingDanRepository.findByZhuangTai(0);

        int closed = 0;
        for (DingDan dingDan : unpaid) {
            if (dingDan.getChuangJianShiJian() != null && dingDan.getChuangJianShiJian().isBefore(deadline)) {
                dingDan.setZhuangTai(3);
                dingDanRepository.save(dingDan);
                closed++;
            }
        }

        if (closed > 0) {
            log.info("[定时任务] 关闭超时未支付订单 {} 笔（超时阈值 {} 分钟）", closed, unpaidOrderTimeoutMinutes);
        }
    }

    // ==================== 2. 租期到期自动完成 ====================

    @Transactional
    @Scheduled(
            initialDelayString = "${app.scheduled.initial-delay-ms:120000}",
            fixedDelayString = "${app.scheduled.finish-expired-interval-ms:600000}")
    public void finishExpiredRentals() {
        LocalDateTime now = LocalDateTime.now();
        List<DingDan> renting = dingDanRepository.findByZhuangTai(1);

        int finished = 0;
        for (DingDan dingDan : renting) {
            LocalDateTime end = dingDan.getZuJieJieShuShiJian();
            if (end == null || end.isAfter(now)) {
                continue;
            }

            dingDan.setZhuangTai(2);
            dingDanRepository.save(dingDan);

            shangPinRepository.findById(dingDan.getShangPinId()).ifPresent(shangPin -> {
                shangPin.setZhuangTai(1);
                shangPinRepository.save(shangPin);
            });

            finished++;
        }

        if (finished > 0) {
            log.info("[定时任务] 租期到期自动完成订单 {} 笔，对应商品已恢复在售", finished);
        }
    }

    // ==================== 3. 清理过期验证码 ====================

    @Scheduled(initialDelayString = "${app.scheduled.initial-delay-ms:120000}", fixedDelay = 600_000L)
    public void cleanExpiredCaptcha() {
        int removed = captchaService.cleanUpExpired();
        if (removed > 0) {
            log.info("[定时任务] 清理过期验证码 {} 条", removed);
        }
    }

    // ==================== 4. 长期挂起的资金待办提醒 ====================

    /** 默认每天 10:00 执行，只告警不自动处理。 */
    @Scheduled(cron = "${app.scheduled.stale-pending-cron:0 0 10 * * *}")
    public void warnStalePendingRequests() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(stalePendingDays);

        long withdraws = tiXianShenQingRepository
                .findByZhuangTaiOrderByShenQingShiJianDesc(0, PageRequest.of(0, 500))
                .stream()
                .filter(t -> t.getShenQingShiJian() != null && t.getShenQingShiJian().isBefore(deadline))
                .count();

        long refunds = tuiKuanShenQingRepository
                .findByZhuangTaiOrderByShenQingShiJianDesc(0, PageRequest.of(0, 500))
                .stream()
                .filter(t -> t.getShenQingShiJian() != null && t.getShenQingShiJian().isBefore(deadline))
                .count();

        if (withdraws > 0 || refunds > 0) {
            log.warn("[定时任务] 存在超过 {} 天未处理的资金申请：提现 {} 笔、退款 {} 笔，请管理员尽快处理",
                    stalePendingDays, withdraws, refunds);
        } else {
            log.debug("[定时任务] 无长期挂起的提现 / 退款申请");
        }
    }
}
