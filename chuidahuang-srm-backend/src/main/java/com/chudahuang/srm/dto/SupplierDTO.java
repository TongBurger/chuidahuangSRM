package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 供应商DTO
 */
@Data
public class SupplierDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private String category;
    private String status;
    private String riskLevel;
    private Integer riskScore;
    private Integer qualityScore;
    private Integer deliveryScore;
    private Integer costScore;
    private Integer innovationScore;
    private BigDecimal overallScore;
    private String contact;
    private String phone;
    private String email;
    private String address;
    private BigDecimal annualSpend;
    private Integer orderCount;
    private BigDecimal onTimeRate;
    private BigDecimal qualityPassRate;

    /**
     * 资质证书列表
     */
    private List<CertificationDTO> certifications;
}
