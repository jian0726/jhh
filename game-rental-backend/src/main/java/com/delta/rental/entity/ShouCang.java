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
@Table(name = "shou_cang")
public class ShouCang {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "yong_hu_id", nullable = false)
  private Long yongHuId;

  @Column(name = "shang_pin_id", nullable = false)
  private Long shangPinId;

  @Column(name = "chuang_jian_shi_jian", nullable = false)
  private LocalDateTime chuangJianShiJian;

  @PrePersist
  protected void onCreate() {
    chuangJianShiJian = LocalDateTime.now();
  }
}