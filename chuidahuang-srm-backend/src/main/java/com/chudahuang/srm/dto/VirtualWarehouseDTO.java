package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 虚拟仓库DTO
 */
@Data
public class VirtualWarehouseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long supplierId;
    private String supplierName;
    private String materialCode;
    private String materialName;
    private BigDecimal theoreticalQty;
    private BigDecimal actualQty;
    private BigDecimal variance;
    private BigDecimal varianceRate;
    private String unit;
    private BigDecimal wipQty;
    private BigDecimal defectQty;
    private BigDecimal finishedQty;
    private LocalDate lastUpdateDate;
    private String remark;
}
