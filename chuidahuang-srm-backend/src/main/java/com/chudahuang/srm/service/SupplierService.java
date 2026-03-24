package com.chudahuang.srm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chudahuang.srm.dto.SupplierDTO;
import com.chudahuang.srm.entity.Supplier;

/**
 * 供应商服务接口
 */
public interface SupplierService extends IService<Supplier> {

    /**
     * 分页查询供应商列表
     */
    Page<SupplierDTO> pageSuppliers(Page<Supplier> page, String name, String category, String riskLevel);

    /**
     * 获取供应商详情
     */
    SupplierDTO getSupplierDetail(Long id);

    /**
     * 新增供应商
     */
    Supplier createSupplier(SupplierDTO supplierDTO);

    /**
     * 更新供应商
     */
    Supplier updateSupplier(Long id, SupplierDTO supplierDTO);

    /**
     * 删除供应商
     */
    boolean deleteSupplier(Long id);

    /**
     * 更新供应商评分
     */
    boolean updateSupplierScore(Long id);

    /**
     * 获取供应商统计信息
     */
    java.util.Map<String, Object> getSupplierStatistics();
}
