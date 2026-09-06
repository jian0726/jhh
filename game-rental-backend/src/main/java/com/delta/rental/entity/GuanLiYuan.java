package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "guanliyuan")
public class GuanLiYuan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guanLiYuanId;

    @Column(nullable = false, length = 50)
    private String caoZuo;

    @Column(length = 100)
    private String muBiaoLeiXing;

    private Long muBiaoId;

    @Column(columnDefinition = "TEXT")
    private String xiQing;

    @Column(nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
    }
}
