package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 虚拟仓库实体
 */
@Data
@TableName("srm_virtual_warehouse")
public class VirtualWarehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余字段）
     */
    private String supplierName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 理论发料数量
     */
    private BigDecimal theoreticalQty;

    /**
     * 实际消耗数量
     */
    private BigDecimal actualQty;

    /**
     * 差异数量
     */
    private BigDecimal variance;

    /**
     * 差异率
     */
    private BigDecimal varianceRate;

    /**
     * 单位
     */
    private String unit;

    /**
     * 在制品数量
     */
    private BigDecimal wipQty;

    /**
     * 残次品数量
     */
    private BigDecimal defectQty;

    /**
     * 成品数量
     */
    private BigDecimal finishedQty;

    /**
     * 最后更新日期
     */
    private java.time.LocalDate lastUpdateDate;

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
