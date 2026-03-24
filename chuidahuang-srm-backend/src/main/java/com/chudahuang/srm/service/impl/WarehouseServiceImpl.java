package com.chudahuang.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.dto.*;
import com.chudahuang.srm.entity.*;
import com.chudahuang.srm.mapper.*;
import com.chudahuang.srm.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仓库服务实现
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final VirtualWarehouseMapper virtualWarehouseMapper;
    private final ASNMapper asnMapper;
    private final ASNItemMapper asnItemMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<com.chudahuang.srm.dto.VirtualWarehouseDTO> pageVirtualWarehouses(Page<VirtualWarehouse> page, Long supplierId) {
        LambdaQueryWrapper<VirtualWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(supplierId != null, VirtualWarehouse::getSupplierId, supplierId)
                .orderByDesc(VirtualWarehouse::getLastUpdateDate);

        Page<VirtualWarehouse> warehousePage = virtualWarehouseMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<com.chudahuang.srm.dto.VirtualWarehouseDTO> dtoPage = new Page<>(
                warehousePage.getCurrent(), warehousePage.getSize(), warehousePage.getTotal());
        List<com.chudahuang.srm.dto.VirtualWarehouseDTO> dtoList = warehousePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public com.chudahuang.srm.dto.VirtualWarehouseDTO getVirtualWarehouseDetail(Long id) {
        VirtualWarehouse warehouse = virtualWarehouseMapper.selectById(id);
        if (warehouse == null) {
            return null;
        }
        return convertToDTO(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVirtualWarehouse(Long id, BigDecimal actualQty) {
        VirtualWarehouse warehouse = virtualWarehouseMapper.selectById(id);
        if (warehouse == null) {
            throw new RuntimeException("虚拟仓库记录不存在");
        }

        warehouse.setActualQty(actualQty);

        // 计算差异数量和差异率
        BigDecimal variance = actualQty.subtract(warehouse.getTheoreticalQty());
        warehouse.setVariance(variance);

        if (warehouse.getTheoreticalQty().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal varianceRate = variance.divide(warehouse.getTheoreticalQty(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            warehouse.setVarianceRate(varianceRate);
        }

        warehouse.setLastUpdateDate(LocalDate.now());
        virtualWarehouseMapper.updateById(warehouse);

        return true;
    }

    @Override
    public Page<ASNDTO> pageASNs(Page<ASN> page, Long supplierId, String status) {
        LambdaQueryWrapper<ASN> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(supplierId != null, ASN::getSupplierId, supplierId)
                .eq(status != null, ASN::getStatus, status)
                .orderByDesc(ASN::getShipDate);

        Page<ASN> asnPage = asnMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<ASNDTO> dtoPage = new Page<>(asnPage.getCurrent(), asnPage.getSize(), asnPage.getTotal());
        List<ASNDTO> dtoList = asnPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public ASNDTO getASNDetail(Long id) {
        ASN asn = asnMapper.selectById(id);
        if (asn == null) {
            return null;
        }
        return convertToDTO(asn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ASN createASN(ASNDTO dto) {
        // 查询供应商信息
        Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }

        ASN asn = new ASN();
        BeanUtil.copyProperties(dto, asn);
        asn.setSupplierName(supplier.getName());

        // 生成ASN编号
        asn.setAsnNo("ASN" + LocalDate.now().toString().replace("-", "") +
                String.format("%04d", System.currentTimeMillis() % 10000));
        asn.setStatus("待发货");

        asnMapper.insert(asn);

        // 保存ASN明细
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ASNItemDTO itemDTO : dto.getItems()) {
                ASNItem item = new ASNItem();
                BeanUtil.copyProperties(itemDTO, item);
                item.setAsnId(asn.getId());
                // 生成条码
                item.setBarcode("BC" + LocalDate.now().toString().replace("-", "") +
                        String.format("%08d", System.currentTimeMillis() % 100000000));
                asnItemMapper.insert(item);
            }
        }

        return asn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateASNStatus(Long id, String status) {
        ASN asn = asnMapper.selectById(id);
        if (asn == null) {
            throw new RuntimeException("ASN不存在");
        }

        asn.setStatus(status);
        if ("已收货".equals(status)) {
            asn.setActualArrival(LocalDate.now());
        }

        asnMapper.updateById(asn);
        return true;
    }

    @Override
    public String printASNLabels(Long id) {
        // TODO: 生成ASN标签PDF
        ASN asn = asnMapper.selectById(id);
        if (asn == null) {
            throw new RuntimeException("ASN不存在");
        }

        // 模拟返回标签URL
        return "/api/labels/asn_" + asn.getAsnNo() + ".pdf";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean scanReceipt(Long id, String barcode) {
        // 验证条码
        ASNItem item = asnItemMapper.selectOne(new LambdaQueryWrapper<ASNItem>()
                .eq(ASNItem::getAsnId, id)
                .eq(ASNItem::getBarcode, barcode));

        if (item == null) {
            throw new RuntimeException("条码不存在");
        }

        // 更新ASN状态
        ASN asn = asnMapper.selectById(id);
        if (asn == null) {
            throw new RuntimeException("ASN不存在");
        }

        // 检查是否所有明细都已收货
        long totalCount = asnItemMapper.selectCount(new LambdaQueryWrapper<ASNItem>().eq(ASNItem::getAsnId, id));
        long receivedCount = asnItemMapper.selectCount(new LambdaQueryWrapper<ASNItem>()
                .eq(ASNItem::getAsnId, id)
                .isNotNull(ASNItem::getActualArrival));

        if (receivedCount + 1 >= totalCount) {
            asn.setStatus("已收货");
            asn.setActualArrival(LocalDate.now());
            asnMapper.updateById(asn);
        }

        return true;
    }

    @Override
    public Map<String, Object> generateReconciliationReport(Long supplierId) {
        LambdaQueryWrapper<VirtualWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(supplierId != null, VirtualWarehouse::getSupplierId, supplierId);

        List<VirtualWarehouse> warehouses = virtualWarehouseMapper.selectList(wrapper);

        Map<String, Object> report = new HashMap<>();
        report.put("totalItems", warehouses.size());
        report.put("varianceAlertCount", warehouses.stream()
                .filter(w -> w.getVarianceRate() != null && w.getVarianceRate().abs().compareTo(new BigDecimal("2")) > 0)
                .count());

        BigDecimal totalVariance = warehouses.stream()
                .map(VirtualWarehouse::getVariance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalVariance", totalVariance);

        report.put("items", warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return report;
    }

    @Override
    public Map<String, Object> getWarehouseStatistics() {
        List<VirtualWarehouse> allWarehouses = virtualWarehouseMapper.selectList(new LambdaQueryWrapper<>());
        List<ASN> allASNs = asnMapper.selectList(new LambdaQueryWrapper<>());

        Map<String, Object> stats = new HashMap<>();
        stats.put("virtualWarehouseCount", allWarehouses.size());
        stats.put("inTransitASN", allASNs.stream().filter(a -> "在途".equals(a.getStatus())).count());
        stats.put("pendingReceiptASN", allASNs.stream().filter(a -> "已到货".equals(a.getStatus())).count());
        stats.put("varianceAlertCount", allWarehouses.stream()
                .filter(w -> w.getVarianceRate() != null && w.getVarianceRate().abs().compareTo(new BigDecimal("2")) > 0)
                .count());

        return stats;
    }

    /**
     * 转换为DTO
     */
    private com.chudahuang.srm.dto.VirtualWarehouseDTO convertToDTO(VirtualWarehouse warehouse) {
        com.chudahuang.srm.dto.VirtualWarehouseDTO dto = new com.chudahuang.srm.dto.VirtualWarehouseDTO();
        BeanUtil.copyProperties(warehouse, dto);
        return dto;
    }

    /**
     * 转换ASN为DTO
     */
    private ASNDTO convertToDTO(ASN asn) {
        ASNDTO dto = new ASNDTO();
        BeanUtil.copyProperties(asn, dto);

        // 查询ASN明细
        List<ASNItemDTO> items = asnItemMapper.selectList(
                new LambdaQueryWrapper<ASNItem>().eq(ASNItem::getAsnId, asn.getId())
        ).stream().map(item -> {
            ASNItemDTO itemDTO = new ASNItemDTO();
            BeanUtil.copyProperties(item, itemDTO);
            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}
