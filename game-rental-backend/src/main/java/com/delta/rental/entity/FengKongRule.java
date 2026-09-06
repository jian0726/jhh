package com.delta.rental.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fengkong_rule")
public class FengKongRule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String ruleCode;

  @Column(nullable = false, length = 100)
  private String ruleName;

  @Column(nullable = false)
  private String ruleType;

  @Column(nullable = false)
  private Integer riskScore;

  @Column(nullable = false)
  private Integer thresholdValue;

  @Column(nullable = false)
  private Integer timeWindow;

  @Column(nullable = false)
  private Boolean enabled = true;

  @Column(length = 200)
  private String description;

  @Column(nullable = false)
  private LocalDateTime createTime;

  @Column(nullable = false)
  private LocalDateTime updateTime;

  @PrePersist
  protected void onCreate() {
    createTime = LocalDateTime.now();
    updateTime = LocalDateTime.now();
    if (enabled == null) {
      enabled = true;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updateTime = LocalDateTime.now();
  }
}
