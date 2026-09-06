package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lunbotu")
public class LunBoTu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tu_pian_url", nullable = false)
    private String tuPianUrl;

    @Column(name = "tie_tle")
    private String tieTle;

    @Column(name = "lian_jie_url")
    private String lianJieUrl;

    @Column(name = "shun_xu")
    @Builder.Default
    private Integer shunXu = 0;

    @Column(name = "zhuang_tai")
    @Builder.Default
    private Integer zhuangTai = 1;

    @Column(name = "chuang_jian_shi_jian")
    private LocalDateTime chuangJianShiJian;

    @Column(name = "geng_xin_shi_jian")
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