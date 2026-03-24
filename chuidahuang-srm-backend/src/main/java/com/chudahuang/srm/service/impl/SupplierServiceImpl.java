package com.chudahuang.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chudahuang.srm.dto.SupplierDTO;
import com.chudahuang.srm.entity.Supplier;
import com.chudahuang.srm.entity.SupplierCertification;
import com.chudahuang.srm.mapper.SupplierCertificationMapper;
import com.chudahuang.srm.mapper.SupplierMapper;
import com.chudahuang.srm.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 供应商服务实现
 */
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private final SupplierCertificationMapper certificationMapper;

    @Override
    public Page<SupplierDTO> pageSuppliers(Page<Supplier> page, String name, String category, String riskLevel) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(name), Supplier::getName, name)
                .eq(StrUtil.isNotBlank(category), Supplier::getCategory, category)
                .eq(StrUtil.isNotBlank(riskLevel), Supplier::getRiskLevel, riskLevel)
                .orderByDesc(Supplier::getCreateTime);

        Page<Supplier> supplierPage = page(page, wrapper);

        // 转换为DTO
        Page<SupplierDTO> dtoPage = new Page<>(supplierPage.getCurrent(), supplierPage.getSize(), supplierPage.getTotal());
        List<SupplierDTO> dtoList = supplierPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public SupplierDTO getSupplierDetail(Long id) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            return null;
        }
        return convertToDTO(supplier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Supplier createSupplier(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        BeanUtil.copyProperties(dto, supplier);

        // 设置默认值
        if (supplier.getStatus() == null) {
            supplier.setStatus("合格");
        }
        if (supplier.getRiskLevel() == null) {
            supplier.setRiskLevel("中");
        }
        if (supplier.getRiskScore() == null) {
            supplier.setRiskScore(50);
        }
        if (supplier.getQualityScore() == null) {
            supplier.setQualityScore(80);
        }
        if (supplier.getDeliveryScore() == null) {
            supplier.setDeliveryScore(80);
        }
        if (supplier.getCostScore() == null) {
            supplier.setCostScore(80);
        }
        if (supplier.getInnovationScore() == null) {
            supplier.setInnovationScore(80);
        }

        // 计算综合评分
        calculateOverallScore(supplier);

        save(supplier);
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Supplier updateSupplier(Long id, SupplierDTO dto) {
        Supplier supplier = getById(id);
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }

        BeanUtil.copyProperties(dto, supplier, "id");
        calculateOverallScore(supplier);

        updateById(supplier);
        return supplier;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSupplier(Long id) {
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSupplierScore(Long id) {
        // TODO: 根据供应商的历史订单数据自动计算评分
        return true;
    }

    @Override
    public Map<String, Object> getSupplierStatistics() {
        List<Supplier> allSuppliers = list();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allSuppliers.size());
        stats.put("strategic", allSuppliers.stream().filter(s -> "战略".equals(s.getCategory())).count());
        stats.put("lowRisk", allSuppliers.stream().filter(s -> "低".equals(s.getRiskLevel())).count());
        stats.put("totalSpend", allSuppliers.stream()
                .map(Supplier::getAnnualSpend)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return stats;
    }

    /**
     * 计算综合评分
     */
    private void calculateOverallScore(Supplier supplier) {
        BigDecimal quality = BigDecimal.valueOf(supplier.getQualityScore() != null ? supplier.getQualityScore() : 80);
        BigDecimal delivery = BigDecimal.valueOf(supplier.getDeliveryScore() != null ? supplier.getDeliveryScore() : 80);
        BigDecimal cost = BigDecimal.valueOf(supplier.getCostScore() != null ? supplier.getCostScore() : 80);
        BigDecimal innovation = BigDecimal.valueOf(supplier.getInnovationScore() != null ? supplier.getInnovationScore() : 80);

        // 权重: 质量40%, 交付30%, 成本20%, 创新10%
        BigDecimal overall = quality.multiply(new BigDecimal("0.4"))
                .add(delivery.multiply(new BigDecimal("0.3")))
                .add(cost.multiply(new BigDecimal("0.2")))
                .add(innovation.multiply(new BigDecimal("0.1")));

        supplier.setOverallScore(overall.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 转换为DTO
     */
    private SupplierDTO convertToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        BeanUtil.copyProperties(supplier, dto);

        // 查询资质证书
        List<com.chudahuang.srm.dto.CertificationDTO> certifications = certificationMapper.selectList(
                new LambdaQueryWrapper<com.chudahuang.srm.entity.SupplierCertification>()
                        .eq(com.chudahuang.srm.entity.SupplierCertification::getSupplierId, supplier.getId())
        ).stream().map(cert -> {
            com.chudahuang.srm.dto.CertificationDTO certDTO = new com.chudahuang.srm.dto.CertificationDTO();
            BeanUtil.copyProperties(cert, certDTO);
            return certDTO;
        }).collect(Collectors.toList());

        dto.setCertifications(certifications);
        return dto;
    }
}
