package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dingdan")
public class DingDan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String dingDanHao;

    @Column(nullable = false)
    private Long shangPinId;

    @Column(nullable = false)
    private Long maiJiaId;

    @Column(nullable = false)
    private Long maiJiaId2;

    @Column(nullable = false)
    private Integer zuJieTianShu;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal zongJinE;

    @Column(nullable = false)
    private Integer zhuangTai = 0;

    private LocalDateTime zuJieKaiShiShiJian;

    private LocalDateTime zuJieJieShuShiJian;

    private String linShiZhangHao;

    private String linShiMiMa;

    private LocalDateTime fuKuanShiJian;

    private LocalDateTime querenShouHuoShiJian;

    @Column(length = 500)
    private String juJueYuanYin;

    @Column(length = 50)
    private String ip;

    @Column(length = 500)
    private String sheBeiShiWen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    private LocalDateTime gengXinShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
        gengXinShiJian = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        gengXinShiJian = LocalDateTime.now();
    }
}
