package com.chudahuang.srm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户DTO
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String name;
    private String role;
    private String department;
    private String status;
    private String phone;
    private String email;
    private Long supplierId;
}
