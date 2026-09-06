package com.delta.rental.dto;

import lombok.Data;

@Data
public class PingJiaRequest {
    private Long dingDanId;
    private Long shangPinId;
    private Integer pingFen;
    private String pingJiaNeiRong;
}
