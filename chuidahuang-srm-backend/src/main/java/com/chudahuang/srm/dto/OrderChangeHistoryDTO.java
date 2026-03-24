package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 订单变更历史DTO
 */
@Data
public class OrderChangeHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Integer version;
    private LocalDate changeDate;
    private String changeType;
    private String description;
    private String operator;
}
