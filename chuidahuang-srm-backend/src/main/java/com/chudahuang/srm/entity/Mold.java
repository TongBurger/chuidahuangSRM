package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 模具实体
 */
@Data
@TableName("srm_mold")
public class Mold implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模具编号
     */
    private String code;

    /**
     * 模具名称
     */
    private String name;

    /**
     * 模具分类：压铸模/注塑模/冲压模等
     */
    private String category;

    /**
     * 状态：在用/维保中/闲置/报废
     */
    private String status;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称（冗余字段）
     */
    private String supplierName;

    /**
     * 模具原值
     */
    private BigDecimal originalValue;

    /**
     * 设计寿命（模次）
     */
    private Long designLife;

    /**
     * 当前使用次数
     */
    private Long currentUsage;

    /**
     * 剩余寿命百分比
     */
    private BigDecimal remainingLife;

    /**
     * 上次维保日期
     */
    private LocalDate lastMaintenanceDate;

    /**
     * 下次维保日期
     */
    private LocalDate nextMaintenanceDate;

    /**
     * 图纸文件列表（JSON）
     */
    private String drawings;

    /**
     * 二维码
     */
    private String qrCode;

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
