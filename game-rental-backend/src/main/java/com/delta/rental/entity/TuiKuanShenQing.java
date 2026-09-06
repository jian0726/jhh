package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tuiKuanShenQing")
public class TuiKuanShenQing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dingDanHao", nullable = false, length = 50)
    private String dingDanHao;

    @Column(name = "yongHuId", nullable = false)
    private Long yongHuId;

    @Column(name = "tuiKuanYuanYin", columnDefinition = "TEXT")
    private String tuiKuanYuanYin;

    @Column(name = "pingZhengUrl", length = 500)
    private String pingZhengUrl;

    @Column(name = "tuiKuanJinE", nullable = false)
    private java.math.BigDecimal tuiKuanJinE;

    @Column(name = "zhuangTai", nullable = false)
    private Integer zhuangTai;

    @Column(name = "maiJiaFuKuan")
    private Integer maiJiaFuKuan;

    @Column(name = "shenQingShiJian", nullable = false)
    private LocalDateTime shenQingShiJian;

    @Column(name = "chuLiShiJian")
    private LocalDateTime chuLiShiJian;

    @Column(name = "guanLiYuanBeiZhu", columnDefinition = "TEXT")
    private String guanLiYuanBeiZhu;

    @PrePersist
    protected void onCreate() {
        shenQingShiJian = LocalDateTime.now();
        zhuangTai = 0;
        maiJiaFuKuan = 0;
    }
}