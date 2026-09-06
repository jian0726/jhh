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
@Table(name = "imHuiHua")
public class ImHuiHua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "huiHuaHao", unique = true, nullable = false, length = 50)
    private String huiHuaHao;

    @Column(name = "yongHuId1", nullable = false)
    private Long yongHuId1;

    @Column(name = "yongHuId2", nullable = false)
    private Long yongHuId2;

    @Column(name = "zuiHouXiaoXiShiJian")
    private LocalDateTime zuiHouXiaoXiShiJian;

    @Column(name = "chuangJianShiJian", nullable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
        zuiHouXiaoXiShiJian = LocalDateTime.now();
    }
}