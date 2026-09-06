package com.delta.rental.service;

import com.delta.rental.dto.DingDanRequest;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.entity.DingDan;
import com.delta.rental.entity.FengXian;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.DingDanRepository;
import com.delta.rental.repository.FengXianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DingDanService {

    private final DingDanRepository dingDanRepository;
    private final ShangPinRepository shangPinRepository;
    private final FengXianRepository fengXianRepository;
    private final FengKongService fengKongService;

    @Transactional
    public DingDan createDingDan(Long maiJiaId2, DingDanRequest request, String ip, String deviceFingerprint) {
        ShangPin shangPin = shangPinRepository.findById(request.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));

        if (shangPin.getZhuangTai() != 1) {
            throw new BusinessException(400, "商品不可租借");
        }

        if (request.getZuJieTianShu() < shangPin.getZuiShaoZuJieTianShu()) {
            throw new BusinessException(400, "租借天数少于最小要求");
        }

        BigDecimal totalAmount = shangPin.getRiZuJiaGe().multiply(BigDecimal.valueOf(request.getZuJieTianShu()));

        FengKongService.FengKongResult riskResult = fengKongService.checkOrderRisk(
                maiJiaId2, request.getShangPinId(), ip, deviceFingerprint, totalAmount);

        if (!riskResult.isPass()) {
            String message = "订单创建被风控拦截，风险等级: " + riskResult.getRiskLevel();
            if (!riskResult.getTriggeredRules().isEmpty()) {
                message += "，触发规则: " + String.join(", ", riskResult.getTriggeredRules());
            }
            throw new BusinessException(403, message);
        }

        DingDan dingDan = new DingDan();
        dingDan.setDingDanHao(generateDingDanHao());
        dingDan.setShangPinId(shangPin.getId());
        dingDan.setMaiJiaId(shangPin.getMaiJiaId());
        dingDan.setMaiJiaId2(maiJiaId2);
        dingDan.setZuJieTianShu(request.getZuJieTianShu());
        dingDan.setZongJinE(totalAmount);
        dingDan.setZhuangTai(0);
        dingDan.setIp(ip);
        dingDan.setSheBeiShiWen(deviceFingerprint);

        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan fuKuanDingDan(String dingDanHao, String ip, String sheBeiShiWen) {
        DingDan dingDan = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (dingDan.getZhuangTai() != 0) {
            throw new BusinessException(400, "订单状态不正确");
        }

        dingDan.setZhuangTai(1);
        dingDan.setFuKuanShiJian(LocalDateTime.now());
        dingDan.setZuJieKaiShiShiJian(LocalDateTime.now());
        dingDan.setZuJieJieShuShiJian(LocalDateTime.now().plusDays(dingDan.getZuJieTianShu()));

        ShangPin shangPin = shangPinRepository.findById(dingDan.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(2);
        shangPinRepository.save(shangPin);

        dingDan.setLinShiZhangHao(generateLinShiZhangHao(shangPin.getYouXiZhangHao()));
        dingDan.setLinShiMiMa(generateLinShiMiMa());

        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan wanChengDingDan(String dingDanHao) {
        DingDan dingDan = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确");
        }

        dingDan.setZhuangTai(2);

        ShangPin shangPin = shangPinRepository.findById(dingDan.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);

        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan quXiaoDingDan(String dingDanHao, Long yongHuId) {
        DingDan dingDan = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!dingDan.getMaiJiaId2().equals(yongHuId)) {
            throw new BusinessException(403, "无权取消此订单");
        }

        if (dingDan.getZhuangTai() != 0) {
            throw new BusinessException(400, "订单已支付，无法取消");
        }

        dingDan.setZhuangTai(3);
        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan jiufengDingDan(String dingDanHao, Long yongHuId, String yuanYin) {
        DingDan dingDan = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        if (!dingDan.getMaiJiaId2().equals(yongHuId) && !dingDan.getMaiJiaId().equals(yongHuId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        dingDan.setZhuangTai(4);

        FengXian fengXian = new FengXian();
        fengXian.setYongHuId(yongHuId);
        fengXian.setFengXianLeiXing("JIUFENG");
        fengXian.setMiaoShu("订单纠纷: " + dingDanHao + ", 原因: " + yuanYin);
        fengXianRepository.save(fengXian);

        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan chuliJiufeng(String dingDanHao) {
        DingDan dingDan = dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));

        dingDan.setZhuangTai(2);

        ShangPin shangPin = shangPinRepository.findById(dingDan.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);

        return dingDanRepository.save(dingDan);
    }

    public DingDan getDingDanByHao(String dingDanHao) {
        return dingDanRepository.findByDingDanHao(dingDanHao)
                .orElseThrow(() -> new BusinessException(404, "订单不存在"));
    }

    public List<DingDan> getMaiJiaDingDan(Long maiJiaId2) {
        return dingDanRepository.findByMaiJiaId2(maiJiaId2);
    }

    public List<DingDan> getMaiJiaDingDanById(Long maiJiaId) {
        return dingDanRepository.findByMaiJiaId(maiJiaId);
    }

    public List<DingDan> getShangPinDingDan(Long shangPinId) {
        return dingDanRepository.findByShangPinId(shangPinId);
    }

    public List<DingDan> getMyOrders(Long userId) {
        return dingDanRepository.findByMaiJiaId2(userId);
    }

    public java.util.Map<String, Object> getCredential(String dingDanHao, Long userId) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        if (!dingDan.getMaiJiaId2().equals(userId)) {
            throw new BusinessException(403, "无权查看此订单凭证");
        }

        if (dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确，无法获取凭证");
        }

        java.util.Map<String, Object> credential = new java.util.HashMap<>();
        credential.put("linShiZhangHao", dingDan.getLinShiZhangHao());
        credential.put("linShiMiMa", dingDan.getLinShiMiMa());
        credential.put("zuJieKaiShiShiJian", dingDan.getZuJieKaiShiShiJian());
        credential.put("zuJieJieShuShiJian", dingDan.getZuJieJieShuShiJian());

        return credential;
    }

    @Transactional
    public DingDan returnOrder(String dingDanHao, Long userId) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        if (!dingDan.getMaiJiaId2().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确，无法归还");
        }

        dingDan.setZhuangTai(2);

        ShangPin shangPin = shangPinRepository.findById(dingDan.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);

        return dingDanRepository.save(dingDan);
    }

    public java.util.Map<String, Object> getRemainingTime(String dingDanHao) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        java.util.Map<String, Object> result = new java.util.HashMap<>();

        if (dingDan.getZuJieJieShuShiJian() == null) {
            result.put("remainingMinutes", 0);
            result.put("remainingHours", 0);
            result.put("remainingDays", 0);
            result.put("isExpired", true);
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = dingDan.getZuJieJieShuShiJian();

        if (now.isAfter(endTime)) {
            result.put("remainingMinutes", 0);
            result.put("remainingHours", 0);
            result.put("remainingDays", 0);
            result.put("isExpired", true);
            return result;
        }

        java.time.Duration duration = java.time.Duration.between(now, endTime);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        result.put("remainingMinutes", minutes);
        result.put("remainingHours", hours);
        result.put("remainingDays", days);
        result.put("isExpired", false);
        result.put("endTime", endTime);

        return result;
    }

    private String generateDingDanHao() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateLinShiZhangHao(String original) {
        return "TEMP_" + original.substring(0, Math.min(3, original.length()))
                + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateLinShiMiMa() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    @Transactional
    public DingDan inspectStart(String dingDanHao, Long userId) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        if (!dingDan.getMaiJiaId2().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确，无法发起验号");
        }

        dingDan.setZhuangTai(5);
        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan confirmOrder(String dingDanHao, Long userId) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        if (!dingDan.getMaiJiaId2().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (dingDan.getZhuangTai() != 5 && dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确，无法确认收货");
        }

        dingDan.setZhuangTai(2);
        dingDan.setQuerenShouHuoShiJian(LocalDateTime.now());

        ShangPin shangPin = shangPinRepository.findById(dingDan.getShangPinId())
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
        shangPin.setZhuangTai(1);
        shangPinRepository.save(shangPin);

        return dingDanRepository.save(dingDan);
    }

    @Transactional
    public DingDan rejectOrder(String dingDanHao, Long userId, String reason) {
        DingDan dingDan = getDingDanByHao(dingDanHao);

        if (!dingDan.getMaiJiaId2().equals(userId)) {
            throw new BusinessException(403, "无权操作此订单");
        }

        if (dingDan.getZhuangTai() != 5 && dingDan.getZhuangTai() != 1) {
            throw new BusinessException(400, "订单状态不正确，无法拒绝收货");
        }

        dingDan.setZhuangTai(4);
        dingDan.setJuJueYuanYin(reason);

        FengXian fengXian = new FengXian();
        fengXian.setYongHuId(userId);
        fengXian.setFengXianLeiXing("JUJUE_SHOUHUO");
        fengXian.setMiaoShu("拒绝收货: " + dingDanHao + ", 原因: " + reason);
        fengXianRepository.save(fengXian);

        return dingDanRepository.save(dingDan);
    }
}
