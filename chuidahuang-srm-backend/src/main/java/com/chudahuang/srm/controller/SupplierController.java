package com.chudahuang.srm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.common.PageResult;
import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.dto.SupplierDTO;
import com.chudahuang.srm.entity.Supplier;
import com.chudahuang.srm.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 供应商控制器
 */
@Tag(name = "供应商管理", description = "供应商相关接口")
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商列表")
    @GetMapping
    public Result<PageResult<SupplierDTO>> pageSuppliers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "供应商名称") @RequestParam(required = false) String name,
            @Parameter(description = "供应商分类") @RequestParam(required = false) String category,
            @Parameter(description = "风险等级") @RequestParam(required = false) String riskLevel) {

        Page<Supplier> page = new Page<>(current, size);
        Page<SupplierDTO> result = supplierService.pageSuppliers(page, name, category, riskLevel);

        return Result.success(PageResult.of(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        ));
    }

    @Operation(summary = "获取供应商详情")
    @GetMapping("/{id}")
    public Result<SupplierDTO> getSupplierDetail(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.getSupplierDetail(id);
        if (supplier == null) {
            return Result.fail("供应商不存在");
        }
        return Result.success(supplier);
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    public Result<Supplier> createSupplier(@RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierService.createSupplier(supplierDTO);
        return Result.success("供应商创建成功", supplier);
    }

    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    public Result<Supplier> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierService.updateSupplier(id, supplierDTO);
        return Result.success("供应商更新成功", supplier);
    }

    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        boolean success = supplierService.deleteSupplier(id);
        return success ? Result.<Void>success("供应商删除成功", null) : Result.fail("删除失败");
    }

    @Operation(summary = "更新供应商评分")
    @PostMapping("/{id}/score")
    public Result<Void> updateSupplierScore(@PathVariable Long id) {
        boolean success = supplierService.updateSupplierScore(id);
        return success ? Result.<Void>success("评分更新成功", null) : Result.fail("更新失败");
    }

    @Operation(summary = "获取供应商统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSupplierStatistics() {
        Map<String, Object> stats = supplierService.getSupplierStatistics();
        return Result.success(stats);
    }
}
