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
@Table(name = "liuLanLiShi")
public class LiuLanLiShi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "yongHuId", nullable = false)
    private Long yongHuId;

    @Column(name = "shangPinId", nullable = false)
    private Long shangPinId;

    @Column(name = "liuLanShiJian", nullable = false)
    private LocalDateTime liuLanShiJian;

    @PrePersist
    protected void onCreate() {
        liuLanShiJian = LocalDateTime.now();
    }
}