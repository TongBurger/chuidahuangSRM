package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 资质证书DTO
 */
@Data
public class CertificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String certificateNo;
    private String type;
    private LocalDate validUntil;
    private String status;
    private String filePath;
}
