package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fengxian")
public class FengXian {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long yongHuId;

    @Column(nullable = false, length = 50)
    private String fengXianLeiXing;

    @Column(columnDefinition = "TEXT")
    private String miaoShu;

    @Column(length = 50)
    private String ip;

    @Column(length = 500)
    private String sheBeiShiWen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
    }
}
