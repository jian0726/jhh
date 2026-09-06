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
@Table(name = "tiXianShenQing")
public class TiXianShenQing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tiXianHao", unique = true, nullable = false, length = 50)
    private String tiXianHao;

    @Column(name = "yongHuId", nullable = false)
    private Long yongHuId;

    @Column(name = "tiXianJinE", nullable = false)
    private java.math.BigDecimal tiXianJinE;

    @Column(name = "shouKuanFangShi", nullable = false, length = 20)
    private String shouKuanFangShi;

    @Column(name = "shouKuanZhangHao", nullable = false, length = 100)
    private String shouKuanZhangHao;

    @Column(name = "shouKuanRen", length = 50)
    private String shouKuanRen;

    @Column(name = "zhuangTai", nullable = false)
    private Integer zhuangTai;

    @Column(name = "shenQingShiJian", nullable = false)
    private LocalDateTime shenQingShiJian;

    @Column(name = "chuLiShiJian")
    private LocalDateTime chuLiShiJian;

    @Column(name = "failReason", length = 500)
    private String failReason;

    @PrePersist
    protected void onCreate() {
        shenQingShiJian = LocalDateTime.now();
        zhuangTai = 0;
    }
}