package com.delta.rental.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "shangpin")
public class ShangPin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mai_jia_id", nullable = false)
    private Long maiJiaId;

    @Column(name = "biao_ti", nullable = false, length = 100)
    private String biaoTi;

    @Column(name = "miao_shu", columnDefinition = "TEXT")
    private String miaoShu;

    /**
     * 游戏账号。属于敏感字段：仅允许写入（上架时提交），任何接口都不对外序列化，
     * 租客只能在订单支付后通过 {@code GET /api/dingdan/{dingDanHao}/credential} 拿到临时凭证。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "you_xi_zhang_hao", length = 100)
    private String youXiZhangHao;

    /**
     * 游戏密码。同 {@link #youXiZhangHao}，禁止对外序列化，避免商品列表/详情接口泄露。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "you_xi_mi_ma", length = 100)
    private String youXiMiMa;

    @Column(name = "zhang_hao_miao_shu", columnDefinition = "TEXT")
    private String zhangHaoMiaoShu;

    @Column(name = "ri_zu_jia_ge", nullable = false, precision = 10, scale = 2)
    private BigDecimal riZuJiaGe;

    @Column(name = "zui_shao_zu_jie_tian_shu", nullable = false)
    private Integer zuiShaoZuJieTianShu = 1;

    @Column(name = "zhuang_tai", nullable = false)
    private Integer zhuangTai = 0;

    @Column(name = "zu_jie_ci_shu", nullable = false)
    private Integer zuJieCiShu = 0;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "fa_bu_shi_jian", nullable = false, updatable = false)
    private LocalDateTime faBuShiJian;

    /**
     * 创建时间。init.sql 中该列为 NOT NULL 且无默认值，
     * 实体若不映射它，JPA 插入时会因缺少该列而报
     * "Field 'chuang_jian_shi_jian' doesn't have a default value"。
     */
    @Column(name = "chuang_jian_shi_jian", nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    @Column(name = "geng_xin_shi_jian")
    private LocalDateTime gengXinShiJian;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (faBuShiJian == null) {
            faBuShiJian = now;
        }
        if (chuangJianShiJian == null) {
            chuangJianShiJian = now;
        }
        gengXinShiJian = now;
    }

    @PreUpdate
    protected void onUpdate() {
        gengXinShiJian = LocalDateTime.now();
    }
}
