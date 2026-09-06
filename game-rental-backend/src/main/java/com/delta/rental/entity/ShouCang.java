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
@Table(name = "shouCang")
public class ShouCang {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "yongHuId", nullable = false)
  private Long yongHuId;

  @Column(name = "shangPinId", nullable = false)
  private Long shangPinId;

  @Column(name = "chuangJianShiJian", nullable = false)
  private LocalDateTime chuangJianShiJian;

  @PrePersist
  protected void onCreate() {
    chuangJianShiJian = LocalDateTime.now();
  }
}