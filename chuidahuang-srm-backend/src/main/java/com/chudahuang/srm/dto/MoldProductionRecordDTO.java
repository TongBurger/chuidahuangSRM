package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 模具生产记录DTO
 */
@Data
public class MoldProductionRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long moldId;
    private String orderId;
    private LocalDate productionDate;
    private Long quantity;
}
