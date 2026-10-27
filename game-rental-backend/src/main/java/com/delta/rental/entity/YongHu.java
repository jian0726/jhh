package com.delta.rental.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "yonghu")
public class YongHu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String yongHuMing;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 100)
    private String miMa;

    @Column(length = 20)
    private String shouJiHao;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 50)
    private String zhenShiXingMing;

    @Column(length = 18)
    private String shenFenZheng;

    @Column(nullable = false)
    private Integer shiFouShiMing = 0;

    @Column(nullable = false)
    private Integer jueSe = 0;

    @Column(nullable = false)
    private Integer zhuangTai = 0;

    @Column(length = 100)
    private String openId;

    @Column(length = 20)
    private String openIdType;

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
