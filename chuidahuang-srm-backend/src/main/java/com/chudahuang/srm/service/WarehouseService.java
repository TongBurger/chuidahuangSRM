package com.chudahuang.srm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chudahuang.srm.dto.ASNDTO;
import com.chudahuang.srm.entity.ASN;
import com.chudahuang.srm.entity.VirtualWarehouse;

import java.util.List;
import java.util.Map;

/**
 * 仓库服务接口
 */
public interface WarehouseService {

    /**
     * 分页查询虚拟仓库库存
     */
    Page<com.chudahuang.srm.dto.VirtualWarehouseDTO> pageVirtualWarehouses(Page<VirtualWarehouse> page, Long supplierId);

    /**
     * 获取虚拟仓库详情
     */
    com.chudahuang.srm.dto.VirtualWarehouseDTO getVirtualWarehouseDetail(Long id);

    /**
     * 更新虚拟仓库库存
     */
    boolean updateVirtualWarehouse(Long id, java.math.BigDecimal actualQty);

    /**
     * 分页查询ASN列表
     */
    Page<ASNDTO> pageASNs(Page<ASN> page, Long supplierId, String status);

    /**
     * 获取ASN详情
     */
    ASNDTO getASNDetail(Long id);

    /**
     * 创建ASN
     */
    ASN createASN(ASNDTO asnDTO);

    /**
     * 更新ASN状态
     */
    boolean updateASNStatus(Long id, String status);

    /**
     * 打印ASN标签
     */
    String printASNLabels(Long id);

    /**
     * 扫码收货
     */
    boolean scanReceipt(Long id, String barcode);

    /**
     * 生成物料核销报告
     */
    Map<String, Object> generateReconciliationReport(Long supplierId);

    /**
     * 获取仓库统计信息
     */
    Map<String, Object> getWarehouseStatistics();
}
