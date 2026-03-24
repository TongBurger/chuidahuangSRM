package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单变更历史实体
 */
@Data
@TableName("srm_order_change_history")
public class OrderChangeHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 变更日期
     */
    private java.time.LocalDate changeDate;

    /**
     * 变更类型：创建订单/数量变更/物料变更/交期变更/状态变更
     */
    private String changeType;

    /**
     * 变更描述
     */
    private String description;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 变更前内容（JSON）
     */
    private String beforeContent;

    /**
     * 变更后内容（JSON）
     */
    private String afterContent;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createTime;
}
