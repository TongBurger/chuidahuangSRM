package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细DTO
 */
@Data
public class OrderItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal price;
    private BigDecimal amount;
}
