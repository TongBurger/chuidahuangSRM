package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * ASN发货通知单实体
 */
@Data
@TableName("srm_asn")
public class ASN implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ASN编号
     */
    private String asnNo;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余字段）
     */
    private String supplierName;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 发货日期
     */
    private LocalDate shipDate;

    /**
     * 预计到达日期
     */
    private LocalDate estimatedArrival;

    /**
     * 实际到达日期
     */
    private LocalDate actualArrival;

    /**
     * 状态：待发货/在途/已到货/已收货
     */
    private String status;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 物流单号
     */
    private String trackingNo;

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

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
}
