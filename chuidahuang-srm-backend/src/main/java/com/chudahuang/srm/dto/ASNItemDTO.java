package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * ASN明细DTO
 */
@Data
public class ASNItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long asnId;
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private String barcode;
    private Integer boxCount;
    private Integer palletCount;
}
