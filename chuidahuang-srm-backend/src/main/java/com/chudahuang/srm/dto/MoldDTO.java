package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 模具DTO
 */
@Data
public class MoldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private String category;
    private String status;
    private String location;
    private Long supplierId;
    private String supplierName;
    private BigDecimal originalValue;
    private Long designLife;
    private Long currentUsage;
    private BigDecimal remainingLife;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private List<String> drawings;
    private String qrCode;

    /**
     * 生产记录列表
     */
    private List<MoldProductionRecordDTO> productionOrders;
}
