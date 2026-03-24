package com.chudahuang.srm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.common.PageResult;
import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.dto.MoldDTO;
import com.chudahuang.srm.entity.Mold;
import com.chudahuang.srm.service.MoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 模具控制器
 */
@Tag(name = "模具管理", description = "模具相关接口")
@RestController
@RequestMapping("/molds")
@RequiredArgsConstructor
public class MoldController {

    private final MoldService moldService;

    @Operation(summary = "分页查询模具列表")
    @GetMapping
    public Result<PageResult<MoldDTO>> pageMolds(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {

        Page<Mold> page = new Page<>(current, size);
        Page<MoldDTO> result = moldService.pageMolds(page, keyword, status);

        return Result.success(PageResult.of(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        ));
    }

    @Operation(summary = "获取模具详情")
    @GetMapping("/{id}")
    public Result<MoldDTO> getMoldDetail(@PathVariable Long id) {
        MoldDTO mold = moldService.getMoldDetail(id);
        if (mold == null) {
            return Result.fail("模具不存在");
        }
        return Result.success(mold);
    }

    @Operation(summary = "创建模具")
    @PostMapping
    public Result<Mold> createMold(@RequestBody MoldDTO moldDTO) {
        Mold mold = moldService.createMold(moldDTO);
        return Result.success("模具创建成功", mold);
    }

    @Operation(summary = "更新模具")
    @PutMapping("/{id}")
    public Result<Mold> updateMold(@PathVariable Long id, @RequestBody MoldDTO moldDTO) {
        Mold mold = moldService.updateMold(id, moldDTO);
        return Result.success("模具更新成功", mold);
    }

    @Operation(summary = "删除模具")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMold(@PathVariable Long id) {
        boolean success = moldService.deleteMold(id);
        return success ? Result.<Void>success("模具删除成功", null) : Result.fail("删除失败");
    }

    @Operation(summary = "更新模具使用次数")
    @PostMapping("/{id}/usage")
    public Result<Void> updateMoldUsage(
            @PathVariable Long id,
            @Parameter(description = "使用数量") @RequestParam Long quantity) {
        boolean success = moldService.updateMoldUsage(id, quantity);
        return success ? Result.<Void>success("使用次数更新成功", null) : Result.fail("更新失败");
    }

    @Operation(summary = "安排维保")
    @PostMapping("/{id}/maintenance")
    public Result<Void> scheduleMaintenance(
            @PathVariable Long id,
            @Parameter(description = "维保日期") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maintenanceDate) {
        boolean success = moldService.scheduleMaintenance(id, maintenanceDate);
        return success ? Result.<Void>success("维保安排成功", null) : Result.fail("安排失败");
    }

    @Operation(summary = "完成维保")
    @PostMapping("/{id}/maintenance/complete")
    public Result<Void> completeMaintenance(@PathVariable Long id) {
        boolean success = moldService.completeMaintenance(id);
        return success ? Result.<Void>success("维保完成", null) : Result.fail("操作失败");
    }

    @Operation(summary = "获取模具统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getMoldStatistics() {
        Map<String, Object> stats = moldService.getMoldStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "获取需要维保预警的模具")
    @GetMapping("/maintenance-alerts")
    public Result<List<MoldDTO>> getMaintenanceAlertMolds() {
        List<MoldDTO> molds = moldService.getMaintenanceAlertMolds();
        return Result.success(molds);
    }
}
