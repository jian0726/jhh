package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pingjia")
public class PingJia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dingDanId;

    @Column(nullable = false)
    private Long shangPinId;

    @Column(nullable = false)
    private Long pingJiaRenId;

    @Column(nullable = false)
    private Integer pingFen;

    @Column(columnDefinition = "TEXT")
    private String pingJiaNeiRong;

    @Column(nullable = false, updatable = false)
    private LocalDateTime chuangJianShiJian;

    @PrePersist
    protected void onCreate() {
        chuangJianShiJian = LocalDateTime.now();
    }
}
