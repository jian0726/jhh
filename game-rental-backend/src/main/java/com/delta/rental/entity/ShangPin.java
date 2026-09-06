package com.delta.rental.entity;

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

    @Column(name = "you_xi_zhang_hao", length = 100)
    private String youXiZhangHao;

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

    @Column(name = "geng_xin_shi_jian")
    private LocalDateTime gengXinShiJian;

    @PrePersist
    protected void onCreate() {
        faBuShiJian = LocalDateTime.now();
        gengXinShiJian = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        gengXinShiJian = LocalDateTime.now();
    }
}
