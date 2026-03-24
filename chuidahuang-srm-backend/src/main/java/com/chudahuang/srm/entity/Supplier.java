package com.chudahuang.srm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 供应商实体
 */
@Data
@TableName("srm_supplier")
public class Supplier implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商名称
     */
    private String name;

    /**
     * 供应商编码
     */
    private String code;

    /**
     * 供应商分类：战略/关键/杠杆/常规
     */
    private String category;

    /**
     * 状态：合格/暂停/黑名单
     */
    private String status;

    /**
     * 风险等级：低/中/高
     */
    private String riskLevel;

    /**
     * 风险评分
     */
    private Integer riskScore;

    /**
     * 质量评分
     */
    private Integer qualityScore;

    /**
     * 交付评分
     */
    private Integer deliveryScore;

    /**
     * 成本评分
     */
    private Integer costScore;

    /**
     * 创新评分
     */
    private Integer innovationScore;

    /**
     * 综合评分
     */
    private BigDecimal overallScore;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 年度采购额
     */
    private BigDecimal annualSpend;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 准时交付率
     */
    private BigDecimal onTimeRate;

    /**
     * 质量合格率
     */
    private BigDecimal qualityPassRate;

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
