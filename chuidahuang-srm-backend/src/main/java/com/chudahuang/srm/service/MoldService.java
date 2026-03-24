package com.chudahuang.srm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chudahuang.srm.dto.MoldDTO;
import com.chudahuang.srm.entity.Mold;

/**
 * 模具服务接口
 */
public interface MoldService extends IService<Mold> {

    /**
     * 分页查询模具列表
     */
    Page<MoldDTO> pageMolds(Page<Mold> page, String keyword, String status);

    /**
     * 获取模具详情
     */
    MoldDTO getMoldDetail(Long id);

    /**
     * 创建模具
     */
    Mold createMold(MoldDTO moldDTO);

    /**
     * 更新模具
     */
    Mold updateMold(Long id, MoldDTO moldDTO);

    /**
     * 删除模具
     */
    boolean deleteMold(Long id);

    /**
     * 更新模具使用次数
     */
    boolean updateMoldUsage(Long moldId, Long quantity);

    /**
     * 安排维保
     */
    boolean scheduleMaintenance(Long moldId, java.time.LocalDate maintenanceDate);

    /**
     * 完成维保
     */
    boolean completeMaintenance(Long moldId);

    /**
     * 获取模具统计信息
     */
    java.util.Map<String, Object> getMoldStatistics();

    /**
     * 获取需要维保的模具
     */
    java.util.List<MoldDTO> getMaintenanceAlertMolds();
}
