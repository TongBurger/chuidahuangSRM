package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 供应商资质证书实体
 */
@Data
@TableName("srm_supplier_certification")
public class SupplierCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 证书名称
     */
    private String name;

    /**
     * 证书编号
     */
    private String certificateNo;

    /**
     * 证书类型
     */
    private String type;

    /**
     * 有效期至
     */
    private LocalDate validUntil;

    /**
     * 状态：有效/即将到期/已过期
     */
    private String status;

    /**
     * 证书文件路径
     */
    private String filePath;

    /**
     * 预警天数
     */
    private Integer alertDays;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private java.time.LocalDateTime updateTime;
}
