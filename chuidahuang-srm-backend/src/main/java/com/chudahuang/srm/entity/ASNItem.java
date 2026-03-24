package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * ASN明细实体
 */
@Data
@TableName("srm_asn_item")
public class ASNItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ASN ID
     */
    private Long asnId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 数量
     */
    private java.math.BigDecimal quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 条码
     */
    private String barcode;

    /**
     * 箱数
     */
    private Integer boxCount;

    /**
     * 托盘数
     */
    private Integer palletCount;

    /**
     * 实际收货时间
     */
    private java.time.LocalDateTime actualArrival;

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
