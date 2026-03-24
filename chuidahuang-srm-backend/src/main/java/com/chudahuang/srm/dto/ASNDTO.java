package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * ASN发货通知单DTO
 */
@Data
public class ASNDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String asnNo;
    private Long supplierId;
    private String supplierName;
    private String orderNo;
    private LocalDate shipDate;
    private LocalDate estimatedArrival;
    private LocalDate actualArrival;
    private String status;
    private String logisticsCompany;
    private String trackingNo;

    /**
     * ASN明细列表
     */
    private List<ASNItemDTO> items;
}
