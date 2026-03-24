package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购订单实体
 */
@Data
@TableName("srm_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余字段）
     */
    private String supplierName;

    /**
     * 订单日期
     */
    private LocalDate orderDate;

    /**
     * 交货日期
     */
    private LocalDate deliveryDate;

    /**
     * 订单状态：待确认/生产中/已发货/已收货/已入库/已完成
     */
    private String status;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * ERP订单号（用友U8）
     */
    private String erpOrderNo;

    /**
     * U8同步状态
     */
    private String erpSyncStatus;

    /**
     * U8同步时间
     */
    private java.time.LocalDateTime erpSyncTime;

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
