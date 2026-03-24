package com.chudahuang.srm.controller;

import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作台控制器
 */
@Tag(name = "工作台", description = "工作台统计数据接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SupplierService supplierService;
    private final OrderService orderService;
    private final MoldService moldService;
    private final WarehouseService warehouseService;

    @Operation(summary = "获取工作台统计数据")
    @GetMapping
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 供应商统计
        data.put("suppliers", supplierService.getSupplierStatistics());

        // 订单统计
        data.put("orders", orderService.getOrderStatistics());

        // 模具统计
        data.put("molds", moldService.getMoldStatistics());

        // 仓库统计
        data.put("warehouse", warehouseService.getWarehouseStatistics());

        // 风险预警
        Map<String, Object> alerts = new HashMap<>();
        alerts.put("maintenanceMolds", moldService.getMaintenanceAlertMolds().size());
        alerts.put("varianceAlerts", warehouseService.getWarehouseStatistics().get("varianceAlertCount"));
        data.put("alerts", alerts);

        return Result.success(data);
    }
}
