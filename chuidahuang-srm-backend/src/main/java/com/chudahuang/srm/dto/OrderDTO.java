package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 订单DTO
 */
@Data
public class OrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private LocalDate deliveryDate;
    private String status;
    private BigDecimal totalAmount;
    private String erpOrderNo;
    private String erpSyncStatus;

    /**
     * 订单明细列表
     */
    private List<OrderItemDTO> items;

    /**
     * 变更历史列表
     */
    private List<OrderChangeHistoryDTO> changeHistory;
}
