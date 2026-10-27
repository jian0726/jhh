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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        shangPin.setRiZuJiaGe(resolveRiZuJiaGe(request));
        shangPin.setZuiShaoZuJieTianShu(
                request.getZuiShaoZuJieTianShu() != null ? request.getZuiShaoZuJieTianShu() : 1);
        // 前端上架页只提交 preRent（预收租金），直接置为已上架，保证发布后即可在商城列表看到
        shangPin.setZhuangTai(1);

        // 序列化游戏专用字段为 metadata JSON
        shangPin.setMetadata(serializeMetadata(request));

        return shangPinRepository.save(shangPin);
    }

    /**
     * 解析租金：前端提交的是 preRent/zuJin 字符串，数据库 ri_zu_jia_ge 为非空列，
     * 因此当 riZuJiaGe 缺失时从 preRent、zuJin 兜底解析，避免 NOT NULL 约束导致上架失败。
     */
    private BigDecimal resolveRiZuJiaGe(ShangPinRequest req) {
        if (req.getRiZuJiaGe() != null) {
            return req.getRiZuJiaGe();
        }
        BigDecimal fromPreRent = parseDecimal(req.getPreRent());
        if (fromPreRent != null) {
            return fromPreRent;
        }
        BigDecimal fromZuJin = parseDecimal(req.getZuJin());
        if (fromZuJin != null) {
            return fromZuJin;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
        if (request.getRiZuJiaGe() != null || request.getPreRent() != null || request.getZuJin() != null) {
            shangPin.setRiZuJiaGe(resolveRiZuJiaGe(request));
        }
        shangPin.setZuiShaoZuJieTianShu(
                request.getZuiShaoZuJieTianShu() != null ? request.getZuiShaoZuJieTianShu() : 1);
        if (request.getPersonalTag() != null || request.getMingCheng() != null) {
            String newTitle = request.getBiaoTi() != null ? request.getBiaoTi() : request.getPersonalTag();
            if (newTitle == null) newTitle = request.getMingCheng();
            if (newTitle != null) shangPin.setBiaoTi(newTitle);
        }
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

    /**
     * 前端筛选结果页提交的富筛选条件（accountType/accountLevel/training/shootingRange/
     * melee/agent/gunSkin/safeBox/rank/priceMin/priceMax）暂时在内存中过滤，
     * 这些字段都存放在 metadata JSON 里，数据量小，实现简单且可读。
     */
    public List<ShangPin> filterShangPinAdvanced(Map<String, Object> filter) {
        List<ShangPin> all = getAllOnShelfShangPin();
        if (filter == null || filter.isEmpty()) {
            return all;
        }
        List<ShangPin> result = new ArrayList<>();
        for (ShangPin sp : all) {
            if (matchFilter(sp, parseMeta(sp.getMetadata()), filter)) {
                result.add(sp);
            }
        }
        return result;
    }

    private boolean matchFilter(ShangPin sp, Map<String, Object> meta, Map<String, Object> filter) {
        // 账号类型：前端选项其实对应区服（QQ / 微信），同时兼容 accountType
        List<Object> accountType = toList(filter.get("accountType"));
        if (!accountType.isEmpty()) {
            String server = firstNonBlank(str(meta, "server"), str(meta, "quFu"));
            String acct = str(meta, "accountType");
            if (!matches(accountType, server) && !matches(accountType, acct)) {
                return false;
            }
        }

        // 账号等级下限
        Integer minLevel = toInt(filter.get("accountLevel"));
        if (minLevel != null) {
            Integer level = toInt(str(meta, "accountLevel"));
            if (level == null || level < minLevel) {
                return false;
            }
        }

        // 等级型筛选（训练中心 / 靶场）
        if (!matchLevel(meta, "trainingLevel", toList(filter.get("training")))) {
            return false;
        }
        if (!matchLevel(meta, "rangeLevel", toList(filter.get("shootingRange")))) {
            return false;
        }

        // 皮肤型筛选：命中任意一个已选皮肤即算匹配
        if (!matchSkin(toList(meta.get("knifeSkins")), toList(filter.get("melee")), true)) {
            return false;
        }
        if (!matchSkin(toList(meta.get("operatorSkins")), toList(filter.get("agent")), false)) {
            return false;
        }
        if (!matchSkin(toList(meta.get("weaponSkins")), toList(filter.get("gunSkin")), false)) {
            return false;
        }

        // 安全箱（前端选项文案与库中略有差异，使用包含匹配）
        List<Object> safeBox = toList(filter.get("safeBox"));
        if (!safeBox.isEmpty()) {
            String value = str(meta, "safeBox");
            if (!matchesLoose(safeBox, value)) {
                return false;
            }
        }

        // 段位
        List<Object> rank = toList(filter.get("rank"));
        if (!rank.isEmpty() && !matches(rank, str(meta, "rank"))) {
            return false;
        }

        // 价格区间（按日租金 riZuJiaGe 过滤）
        BigDecimal price = sp.getRiZuJiaGe() != null ? sp.getRiZuJiaGe() : BigDecimal.ZERO;
        BigDecimal min = toDecimal(filter.get("priceMin"));
        BigDecimal max = toDecimal(filter.get("priceMax"));
        if (min != null && price.compareTo(min) < 0) {
            return false;
        }
        if (max != null && price.compareTo(max) > 0) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMeta(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> meta = new Gson().fromJson(json, Map.class);
            return meta != null ? meta : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private boolean matchLevel(Map<String, Object> meta, String key, List<Object> options) {
        if (options.isEmpty()) {
            return true;
        }
        Integer level = toInt(str(meta, key));
        if (level == null) {
            return false;
        }
        for (Object option : options) {
            String text = String.valueOf(option);
            Integer optionLevel = toInt(text);
            if (optionLevel == null) {
                continue;
            }
            if (text.contains("及以上")) {
                if (level >= optionLevel) {
                    return true;
                }
            } else if (level.equals(optionLevel)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchSkin(List<Object> skins, List<Object> options, boolean stripPrefix) {
        if (options.isEmpty()) {
            return true;
        }
        for (Object option : options) {
            String target = String.valueOf(option);
            if (stripPrefix) {
                target = target.replace("近战-", "");
            }
            for (Object skin : skins) {
                String value = String.valueOf(skin);
                if (value.contains(target) || target.contains(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Object> toList(Object value) {
        List<Object> list = new ArrayList<>();
        if (value instanceof List<?> raw) {
            for (Object item : raw) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    list.add(item);
                }
            }
        } else if (value != null && !String.valueOf(value).isBlank()) {
            list.add(value);
        }
        return list;
    }

    private boolean matches(List<Object> options, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (Object option : options) {
            if (String.valueOf(option).equals(value.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesLoose(List<Object> options, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (Object option : options) {
            String text = String.valueOf(option);
            if (text.contains(value) || value.contains(text)) {
                return true;
            }
        }
        return false;
    }

    private String str(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).replaceAll("[^0-9-]", "");
        if (text.isBlank() || "-".equals(text)) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
