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
@Table(name = "qianBao")
public class QianBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "yongHuId", unique = true, nullable = false)
    private Long yongHuId;

    @Column(name = "yuE", nullable = false)
    private java.math.BigDecimal yuE;

    @Column(name = "keTiXianJinE", nullable = false)
    private java.math.BigDecimal keTiXianJinE;

    @Column(name = "chuangJianShiJian", nullable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
        if (yuE == null) {
            yuE = java.math.BigDecimal.ZERO;
        }
        if (keTiXianJinE == null) {
            keTiXianJinE = java.math.BigDecimal.ZERO;
        }
    }
}