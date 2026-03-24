package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 模具生产记录实体
 */
@Data
@TableName("srm_mold_production_record")
public class MoldProductionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模具ID
     */
    private Long moldId;

    /**
     * 生产订单号
     */
    private String orderId;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 生产数量
     */
    private Long quantity;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
}
