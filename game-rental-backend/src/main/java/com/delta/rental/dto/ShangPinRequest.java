package com.delta.rental.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ShangPinRequest {
    // 核心字段
    private String biaoTi;
    private String miaoShu;
    private BigDecimal riZuJiaGe;
    private Integer zuiShaoZuJieTianShu;
    private String youXiZhangHao;
    private String youXiMiMa;
    private String zhangHaoMiaoShu;

    // 前端展示字段
    private String personalTag;
    private String coverImageUrl;

    // 游戏专用字段（存储到 metadata JSON）
    private String accountType;
    private String server;
    private String settlementType;
    private String banRecord;
    private String faceSelf;
    private String warehouseAssets;
    private String pureCoinM;
    private String trainingLevel;
    private String rangeLevel;
    private String safeBox;
    private String rank;
    private String accountLevel;
    private String awmBullets;
    private String sixHead;
    private String sixArmor;
    private String secretKD;
    private List<String> knifeSkins;
    private List<String> operatorSkins;
    private List<String> weaponSkins;
    private String ownerBanned;
    private String ownerGifted;
    private String remark;
    private String tradeStartHour;
    private String tradeEndHour;
    private String selfCheck;
    private String settlementRatio;
    private String quickRatioValue;
    private String preRent;
    private String deposit;
    private String halfCoinRatio;

    // 兼容旧字段
    private String tuPianUrl;
    private String zongZiChan;
    private String quFu;
    private String zuJin;
    private String mingCheng;
    private String leiBie;
}
