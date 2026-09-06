package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "xiaoxi")
public class XiaoXi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long yongHuId;

    @Column(nullable = false, length = 100)
    private String biaoTi;

    @Column(columnDefinition = "TEXT")
    private String neiRong;

    @Column(nullable = false)
    private Integer leiXing = 0;

    @Column(nullable = false)
    private Integer shiFouYiDu = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
    }
}
