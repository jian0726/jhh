package com.delta.rental.service;

import com.delta.rental.dto.ShangPinRequest;
import com.delta.rental.entity.ShangPin;
import com.delta.rental.entity.FengXian;
import com.delta.rental.exception.BusinessException;
import com.delta.rental.repository.ShangPinRepository;
import com.delta.rental.repository.FengXianRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShangPinService {

    private final ShangPinRepository shangPinRepository;
    private final FengXianRepository fengXianRepository;

    @Transactional
    public ShangPin createShangPin(Long maiJiaId, ShangPinRequest request) {
        ShangPin shangPin = new ShangPin();
        shangPin.setMaiJiaId(maiJiaId);

        // 映射前端字段到实体
        String title = request.getBiaoTi();
        if (title == null || title.isBlank()) {
            title = request.getPersonalTag();
        }
        if (title == null || title.isBlank()) {
            title = request.getMingCheng();
        }
        shangPin.setBiaoTi(title != null ? title : "游戏账号");
        shangPin.setMiaoShu(request.getMiaoShu());
        shangPin.setYouXiZhangHao(request.getYouXiZhangHao());
        shangPin.setYouXiMiMa(request.getYouXiMiMa());
        shangPin.setZhangHaoMiaoShu(request.getZhangHaoMiaoShu());
        shangPin.setRiZuJiaGe(request.getRiZuJiaGe());
        shangPin.setZuiShaoZuJieTianShu(
                request.getZuiShaoZuJieTianShu() != null ? request.getZuiShaoZuJieTianShu() : 1);
        shangPin.setZhuangTai(0);

        // 序列化游戏专用字段为 metadata JSON
        shangPin.setMetadata(serializeMetadata(request));

        return shangPinRepository.save(shangPin);
    }

    /**
     * 将 ShangPinRequest 中的游戏专用字段序列化为 JSON
     */
    private String serializeMetadata(ShangPinRequest req) {
        Map<String, Object> meta = new HashMap<>();
        if (req.getCoverImageUrl() != null) meta.put("coverImageUrl", req.getCoverImageUrl());
        if (req.getTuPianUrl() != null) meta.put("tuPianUrl", req.getTuPianUrl());
        if (req.getAccountType() != null) meta.put("accountType", req.getAccountType());
        if (req.getServer() != null) meta.put("server", req.getServer());
        if (req.getSettlementType() != null) meta.put("settlementType", req.getSettlementType());
        if (req.getBanRecord() != null) meta.put("banRecord", req.getBanRecord());
        if (req.getFaceSelf() != null) meta.put("faceSelf", req.getFaceSelf());
        if (req.getWarehouseAssets() != null) meta.put("warehouseAssets", req.getWarehouseAssets());
        if (req.getPureCoinM() != null) meta.put("pureCoinM", req.getPureCoinM());
        if (req.getTrainingLevel() != null) meta.put("trainingLevel", req.getTrainingLevel());
        if (req.getRangeLevel() != null) meta.put("rangeLevel", req.getRangeLevel());
        if (req.getSafeBox() != null) meta.put("safeBox", req.getSafeBox());
        if (req.getRank() != null) meta.put("rank", req.getRank());
        if (req.getAccountLevel() != null) meta.put("accountLevel", req.getAccountLevel());
        if (req.getAwmBullets() != null) meta.put("awmBullets", req.getAwmBullets());
        if (req.getSixHead() != null) meta.put("sixHead", req.getSixHead());
        if (req.getSixArmor() != null) meta.put("sixArmor", req.getSixArmor());
        if (req.getSecretKD() != null) meta.put("secretKD", req.getSecretKD());
        if (req.getKnifeSkins() != null) meta.put("knifeSkins", req.getKnifeSkins());
        if (req.getOperatorSkins() != null) meta.put("operatorSkins", req.getOperatorSkins());
        if (req.getWeaponSkins() != null) meta.put("weaponSkins", req.getWeaponSkins());
        if (req.getOwnerBanned() != null) meta.put("ownerBanned", req.getOwnerBanned());
        if (req.getOwnerGifted() != null) meta.put("ownerGifted", req.getOwnerGifted());
        if (req.getRemark() != null) meta.put("remark", req.getRemark());
        if (req.getPersonalTag() != null) meta.put("personalTag", req.getPersonalTag());
        if (req.getTradeStartHour() != null) meta.put("tradeStartHour", req.getTradeStartHour());
        if (req.getTradeEndHour() != null) meta.put("tradeEndHour", req.getTradeEndHour());
        if (req.getSelfCheck() != null) meta.put("selfCheck", req.getSelfCheck());
        if (req.getSettlementRatio() != null) meta.put("settlementRatio", req.getSettlementRatio());
        if (req.getQuickRatioValue() != null) meta.put("quickRatioValue", req.getQuickRatioValue());
        if (req.getPreRent() != null) meta.put("preRent", req.getPreRent());
        if (req.getDeposit() != null) meta.put("deposit", req.getDeposit());
        if (req.getHalfCoinRatio() != null) meta.put("halfCoinRatio", req.getHalfCoinRatio());
        if (req.getZongZiChan() != null) meta.put("zongZiChan", req.getZongZiChan());
        if (req.getQuFu() != null) meta.put("quFu", req.getQuFu());
        if (req.getZuJin() != null) meta.put("zuJin", req.getZuJin());
        if (req.getMingCheng() != null) meta.put("mingCheng", req.getMingCheng());
        if (req.getLeiBie() != null) meta.put("leiBie", req.getLeiBie());
        return new Gson().toJson(meta);
    }

    /**
     * 将实体转换为前端期望的 Map 格式（展开 metadata JSON）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toFrontendFormat(ShangPin shangPin) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", shangPin.getId());
        result.put("biaoTi", shangPin.getBiaoTi());
        result.put("mingCheng", shangPin.getBiaoTi());
        result.put("personalTag", shangPin.getBiaoTi());
        result.put("miaoShu", shangPin.getMiaoShu());
        result.put("riZuJiaGe", shangPin.getRiZuJiaGe());
        result.put("zuJin", shangPin.getRiZuJiaGe());
        result.put("preRent", shangPin.getRiZuJiaGe());
        result.put("zuiShaoZuJieTianShu", shangPin.getZuiShaoZuJieTianShu());
        result.put("zhuangTai", shangPin.getZhuangTai());
        result.put("maiJiaId", shangPin.getMaiJiaId());
        result.put("zuJieCiShu", shangPin.getZuJieCiShu());
        result.put("faBuShiJian", shangPin.getFaBuShiJian() != null ? shangPin.getFaBuShiJian().toString() : null);

        // 展开 metadata JSON
        if (shangPin.getMetadata() != null && !shangPin.getMetadata().isBlank()) {
            try {
                Map<String, Object> meta = new Gson().fromJson(shangPin.getMetadata(), Map.class);
                result.putAll(meta);
            } catch (Exception e) {
                // metadata 解析失败时忽略
            }
        }
        return result;
    }

    public ShangPin getShangPinById(Long id) {
        return shangPinRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "商品不存在"));
    }

    public List<ShangPin> getAllOnShelfShangPin() {
        return shangPinRepository.findByZhuangTai(1);
    }

    public List<ShangPin> getMaiJiaShangPin(Long maiJiaId) {
        return shangPinRepository.findByMaiJiaId(maiJiaId);
    }

    public ShangPin updateShangPin(Long shangPinId, Long maiJiaId, ShangPinRequest request) {
        ShangPin shangPin = getShangPinById(shangPinId);
        if (!shangPin.getMaiJiaId().equals(maiJiaId)) {
            throw new BusinessException(403, "无权操作此商品");
        }

        shangPin.setBiaoTi(request.getBiaoTi());
        shangPin.setMiaoShu(request.getMiaoShu());
        shangPin.setYouXiZhangHao(request.getYouXiZhangHao());
        shangPin.setYouXiMiMa(request.getYouXiMiMa());
        shangPin.setZhangHaoMiaoShu(request.getZhangHaoMiaoShu());
        shangPin.setRiZuJiaGe(request.getRiZuJiaGe());
        shangPin.setZuiShaoZuJieTianShu(
                request.getZuiShaoZuJieTianShu() != null ? request.getZuiShaoZuJieTianShu() : 1);
        return shangPinRepository.save(shangPin);
    }

    public void deleteShangPin(Long shangPinId, Long maiJiaId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        if (!shangPin.getMaiJiaId().equals(maiJiaId)) {
            throw new BusinessException(403, "无权操作此商品");
        }
        shangPinRepository.delete(shangPin);
    }

    public ShangPin tongGuoShenHe(Long shangPinId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        if (shangPin.getZhuangTai() != 0) {
            throw new BusinessException(400, "商品状态不是待审核");
        }
        shangPin.setZhuangTai(1);
        return shangPinRepository.save(shangPin);
    }

    public ShangPin juJueShenHe(Long shangPinId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        if (shangPin.getZhuangTai() != 0) {
            throw new BusinessException(400, "商品状态不是待审核");
        }
        shangPin.setZhuangTai(3);
        return shangPinRepository.save(shangPin);
    }

    public ShangPin markAsRented(Long shangPinId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        shangPin.setZhuangTai(2);
        shangPin.setZuJieCiShu(shangPin.getZuJieCiShu() + 1);
        return shangPinRepository.save(shangPin);
    }

    public ShangPin markAsAvailable(Long shangPinId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        shangPin.setZhuangTai(1);
        return shangPinRepository.save(shangPin);
    }

    public ShangPin xiaJia(Long shangPinId, Long maiJiaId) {
        ShangPin shangPin = getShangPinById(shangPinId);
        if (!shangPin.getMaiJiaId().equals(maiJiaId)) {
            throw new BusinessException(403, "无权操作此商品");
        }
        shangPin.setZhuangTai(3);
        return shangPinRepository.save(shangPin);
    }

    public void logFengXian(Long yongHuId, String fengXianLeiXing, String miaoShu, String ip, String sheBeiShiWen) {
        FengXian log = new FengXian();
        log.setYongHuId(yongHuId);
        log.setFengXianLeiXing(fengXianLeiXing);
        log.setMiaoShu(miaoShu);
        log.setIp(ip);
        log.setSheBeiShiWen(sheBeiShiWen);
        fengXianRepository.save(log);
    }

    public List<ShangPin> searchShangPin(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllOnShelfShangPin();
        }
        return shangPinRepository.searchByKeyword(keyword.trim());
    }

    public List<ShangPin> searchShangPinBoolean(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllOnShelfShangPin();
        }
        return shangPinRepository.searchByKeywordBoolean(keyword.trim());
    }

    public List<ShangPin> filterShangPin(Integer minPrice, Integer maxPrice, Integer zuiShaoZuJieTianShu) {
        return shangPinRepository.filterShangPin( minPrice, maxPrice, zuiShaoZuJieTianShu);
    }
}
