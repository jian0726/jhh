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
@Table(name = "imXiaoXi")
public class ImXiaoXi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "huiHuaHao", nullable = false, length = 50)
    private String huiHuaHao;

    @Column(name = "faSongRenId", nullable = false)
    private Long faSongRenId;

    @Column(name = "shouDaoRenId", nullable = false)
    private Long shouDaoRenId;

    @Column(name = "neiRong", columnDefinition = "TEXT")
    private String neiRong;

    @Column(name = "xiaoXiLeiXing", nullable = false, length = 20)
    private String xiaoXiLeiXing;

    @Column(name = "shiFouYiDu", nullable = false)
    private Integer shiFouYiDu;

    @Column(name = "faSongShiJian", nullable = false)
    private LocalDateTime faSongShiJian;

    @PrePersist
    protected void onCreate() {
        faSongShiJian = LocalDateTime.now();
        if (shiFouYiDu == null) {
            shiFouYiDu = 0;
        }
    }
}