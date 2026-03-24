package com.chudahuang.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chudahuang.srm.dto.MoldDTO;
import com.chudahuang.srm.dto.MoldProductionRecordDTO;
import com.chudahuang.srm.entity.Mold;
import com.chudahuang.srm.entity.MoldProductionRecord;
import com.chudahuang.srm.mapper.MoldMapper;
import com.chudahuang.srm.mapper.MoldProductionRecordMapper;
import com.chudahuang.srm.mapper.SupplierMapper;
import com.chudahuang.srm.service.MoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模具服务实现
 */
@Service
@RequiredArgsConstructor
public class MoldServiceImpl extends ServiceImpl<MoldMapper, Mold> implements MoldService {

    private final MoldProductionRecordMapper productionRecordMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<MoldDTO> pageMolds(Page<Mold> page, String keyword, String status) {
        LambdaQueryWrapper<Mold> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(keyword), w -> w
                        .like(Mold::getCode, keyword)
                        .or()
                        .like(Mold::getName, keyword))
                .eq(StrUtil.isNotBlank(status), Mold::getStatus, status)
                .orderByDesc(Mold::getCreateTime);

        Page<Mold> moldPage = page(page, wrapper);

        // 转换为DTO
        Page<MoldDTO> dtoPage = new Page<>(moldPage.getCurrent(), moldPage.getSize(), moldPage.getTotal());
        List<MoldDTO> dtoList = moldPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public MoldDTO getMoldDetail(Long id) {
        Mold mold = getById(id);
        if (mold == null) {
            return null;
        }
        return convertToDTO(mold);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Mold createMold(MoldDTO dto) {
        // 查询供应商信息
        com.chudahuang.srm.entity.Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierMapper.selectById(dto.getSupplierId());
        }

        Mold mold = new Mold();
        BeanUtil.copyProperties(dto, mold);
        if (supplier != null) {
            mold.setSupplierName(supplier.getName());
        }

        // 设置默认值
        if (mold.getStatus() == null) {
            mold.setStatus("在用");
        }
        if (mold.getCurrentUsage() == null) {
            mold.setCurrentUsage(0L);
        }

        // 计算剩余寿命
        calculateRemainingLife(mold);

        // 生成二维码
        mold.setQrCode("MOLD-" + System.currentTimeMillis());

        save(mold);
        return mold;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Mold updateMold(Long id, MoldDTO dto) {
        Mold mold = getById(id);
        if (mold == null) {
            throw new RuntimeException("模具不存在");
        }

        // 查询供应商信息
        com.chudahuang.srm.entity.Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supplierMapper.selectById(dto.getSupplierId());
        }

        BeanUtil.copyProperties(dto, mold, "id", "qrCode");
        if (supplier != null) {
            mold.setSupplierName(supplier.getName());
        }

        calculateRemainingLife(mold);
        updateById(mold);

        return mold;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMold(Long id) {
        // 删除生产记录
        productionRecordMapper.delete(new LambdaQueryWrapper<MoldProductionRecord>().eq(MoldProductionRecord::getMoldId, id));
        // 删除模具
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMoldUsage(Long moldId, Long quantity) {
        Mold mold = getById(moldId);
        if (mold == null) {
            throw new RuntimeException("模具不存在");
        }

        mold.setCurrentUsage(mold.getCurrentUsage() + quantity);
        calculateRemainingLife(mold);
        updateById(mold);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean scheduleMaintenance(Long moldId, LocalDate maintenanceDate) {
        Mold mold = getById(moldId);
        if (mold == null) {
            throw new RuntimeException("模具不存在");
        }

        mold.setStatus("维保中");
        mold.setNextMaintenanceDate(maintenanceDate);
        updateById(mold);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeMaintenance(Long moldId) {
        Mold mold = getById(moldId);
        if (mold == null) {
            throw new RuntimeException("模具不存在");
        }

        mold.setStatus("在用");
        mold.setLastMaintenanceDate(LocalDate.now());
        // 设置下次维保时间（3个月后）
        mold.setNextMaintenanceDate(LocalDate.now().plusMonths(3));
        updateById(mold);

        return true;
    }

    @Override
    public Map<String, Object> getMoldStatistics() {
        List<Mold> allMolds = list();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allMolds.size());
        stats.put("active", allMolds.stream().filter(m -> "在用".equals(m.getStatus())).count());
        stats.put("maintenance", allMolds.stream().filter(m -> "维保中".equals(m.getStatus())).count());
        stats.put("idle", allMolds.stream().filter(m -> "闲置".equals(m.getStatus())).count());
        stats.put("scrapped", allMolds.stream().filter(m -> "报废".equals(m.getStatus())).count());
        stats.put("lowLife", allMolds.stream().filter(m -> {
            BigDecimal life = m.getRemainingLife();
            return life != null && life.compareTo(new BigDecimal("15")) < 0;
        }).count());

        return stats;
    }

    @Override
    public List<MoldDTO> getMaintenanceAlertMolds() {
        // 查询剩余寿命小于15%的模具
        LambdaQueryWrapper<Mold> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Mold::getRemainingLife, 15)
                .eq(Mold::getStatus, "在用")
                .orderByAsc(Mold::getRemainingLife);

        return list(wrapper).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 计算剩余寿命百分比
     */
    private void calculateRemainingLife(Mold mold) {
        if (mold.getDesignLife() != null && mold.getDesignLife() > 0) {
            Long currentUsage = mold.getCurrentUsage() != null ? mold.getCurrentUsage() : 0L;
            BigDecimal remaining = BigDecimal.valueOf(mold.getDesignLife() - currentUsage)
                    .multiply(new BigDecimal("100"))
                    .divide(BigDecimal.valueOf(mold.getDesignLife()), 2, RoundingMode.HALF_UP);
            mold.setRemainingLife(remaining.max(BigDecimal.ZERO));
        } else {
            mold.setRemainingLife(BigDecimal.ZERO);
        }
    }

    /**
     * 转换为DTO
     */
    private MoldDTO convertToDTO(Mold mold) {
        MoldDTO dto = new MoldDTO();
        BeanUtil.copyProperties(mold, dto);

        // 解析图纸列表
        if (mold.getDrawings() != null) {
            dto.setDrawings(java.util.Arrays.asList(mold.getDrawings().split(",")));
        }

        // 查询生产记录
        List<MoldProductionRecordDTO> records = productionRecordMapper.selectList(
                new LambdaQueryWrapper<MoldProductionRecord>().eq(MoldProductionRecord::getMoldId, mold.getId())
                        .orderByDesc(MoldProductionRecord::getProductionDate)
                        .last("LIMIT 10")
        ).stream().map(record -> {
            MoldProductionRecordDTO recordDTO = new MoldProductionRecordDTO();
            BeanUtil.copyProperties(record, recordDTO);
            return recordDTO;
        }).collect(Collectors.toList());
        dto.setProductionOrders(records);

        return dto;
    }
}
