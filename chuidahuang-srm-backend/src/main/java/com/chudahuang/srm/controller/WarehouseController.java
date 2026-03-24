package com.chudahuang.srm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.common.PageResult;
import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.dto.ASNDTO;
import com.chudahuang.srm.dto.VirtualWarehouseDTO;
import com.chudahuang.srm.entity.ASN;
import com.chudahuang.srm.entity.VirtualWarehouse;
import com.chudahuang.srm.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 仓库控制器
 */
@Tag(name = "代加工仓储", description = "虚拟仓库、ASN相关接口")
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "分页查询虚拟仓库库存")
    @GetMapping("/virtual")
    public Result<PageResult<VirtualWarehouseDTO>> pageVirtualWarehouses(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId) {

        Page<VirtualWarehouse> page = new Page<>(current, size);
        Page<VirtualWarehouseDTO> result = warehouseService.pageVirtualWarehouses(page, supplierId);

        return Result.success(PageResult.of(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        ));
    }

    @Operation(summary = "获取虚拟仓库详情")
    @GetMapping("/virtual/{id}")
    public Result<VirtualWarehouseDTO> getVirtualWarehouseDetail(@PathVariable Long id) {
        VirtualWarehouseDTO warehouse = warehouseService.getVirtualWarehouseDetail(id);
        if (warehouse == null) {
            return Result.fail("虚拟仓库记录不存在");
        }
        return Result.success(warehouse);
    }

    @Operation(summary = "更新虚拟仓库库存")
    @PutMapping("/virtual/{id}")
    public Result<Void> updateVirtualWarehouse(
            @PathVariable Long id,
            @Parameter(description = "实际数量") @RequestParam java.math.BigDecimal actualQty) {
        boolean success = warehouseService.updateVirtualWarehouse(id, actualQty);
        return success ? Result.<Void>success("库存更新成功", null) : Result.fail("更新失败");
    }

    @Operation(summary = "分页查询ASN列表")
    @GetMapping("/asn")
    public Result<PageResult<ASNDTO>> pageASNs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "状态") @RequestParam(required = false) String status) {

        Page<ASN> page = new Page<>(current, size);
        Page<ASNDTO> result = warehouseService.pageASNs(page, supplierId, status);

        return Result.success(PageResult.of(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        ));
    }

    @Operation(summary = "获取ASN详情")
    @GetMapping("/asn/{id}")
    public Result<ASNDTO> getASNDetail(@PathVariable Long id) {
        ASNDTO asn = warehouseService.getASNDetail(id);
        if (asn == null) {
            return Result.fail("ASN不存在");
        }
        return Result.success(asn);
    }

    @Operation(summary = "创建ASN")
    @PostMapping("/asn")
    public Result<ASN> createASN(@RequestBody ASNDTO asnDTO) {
        ASN asn = warehouseService.createASN(asnDTO);
        return Result.success("ASN创建成功", asn);
    }

    @Operation(summary = "更新ASN状态")
    @PutMapping("/asn/{id}/status")
    public Result<Void> updateASNStatus(
            @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        boolean success = warehouseService.updateASNStatus(id, status);
        return success ? Result.<Void>success("状态更新成功", null) : Result.fail("更新失败");
    }

    @Operation(summary = "打印ASN标签")
    @GetMapping("/asn/{id}/print-labels")
    public Result<String> printASNLabels(@PathVariable Long id) {
        String labelUrl = warehouseService.printASNLabels(id);
        return Result.success("标签生成成功", labelUrl);
    }

    @Operation(summary = "扫码收货")
    @PostMapping("/asn/{id}/scan-receipt")
    public Result<Void> scanReceipt(
            @PathVariable Long id,
            @Parameter(description = "条码") @RequestParam String barcode) {
        boolean success = warehouseService.scanReceipt(id, barcode);
        return success ? Result.<Void>success("收货成功", null) : Result.fail("收货失败");
    }

    @Operation(summary = "生成物料核销报告")
    @GetMapping("/reconciliation")
    public Result<Map<String, Object>> generateReconciliationReport(
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId) {
        Map<String, Object> report = warehouseService.generateReconciliationReport(supplierId);
        return Result.success(report);
    }

    @Operation(summary = "获取仓库统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getWarehouseStatistics() {
        Map<String, Object> stats = warehouseService.getWarehouseStatistics();
        return Result.success(stats);
    }
}
