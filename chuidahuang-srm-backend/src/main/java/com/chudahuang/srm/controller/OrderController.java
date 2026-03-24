package com.chudahuang.srm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.common.PageResult;
import com.chudahuang.srm.common.Result;
import com.chudahuang.srm.dto.OrderDTO;
import com.chudahuang.srm.entity.Order;
import com.chudahuang.srm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单控制器
 */
@Tag(name = "订单协同", description = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping
    public Result<PageResult<OrderDTO>> pageOrders(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "订单编号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status) {

        Page<Order> page = new Page<>(current, size);
        Page<OrderDTO> result = orderService.pageOrders(page, orderNo, supplierId, status);

        return Result.success(PageResult.of(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getRecords()
        ));
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<OrderDTO> getOrderDetail(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        return Result.success(order);
    }

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> createOrder(@RequestBody OrderDTO orderDTO) {
        Order order = orderService.createOrder(orderDTO);
        return Result.success("订单创建成功", order);
    }

    @Operation(summary = "更新订单")
    @PutMapping("/{id}")
    public Result<Order> updateOrder(@PathVariable Long id, @RequestBody OrderDTO orderDTO) {
        Order order = orderService.updateOrder(id, orderDTO);
        return Result.success("订单更新成功", order);
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        boolean success = orderService.deleteOrder(id);
        return success ? Result.<Void>success("订单删除成功", null) : Result.fail("删除失败");
    }

    @Operation(summary = "供应商确认订单")
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmOrder(
            @PathVariable Long id,
            @Parameter(description = "是否确认") @RequestParam Boolean confirm,
            @Parameter(description = "拒绝原因") @RequestParam(required = false) String reason) {
        boolean success = orderService.confirmOrder(id, confirm, reason);
        return success ? Result.<Void>success(confirm ? "订单确认成功" : "订单已拒绝", null) : Result.fail("操作失败");
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable Long id,
            @Parameter(description = "状态") @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(id, status);
        return success ? Result.<Void>success("状态更新成功", null) : Result.fail("更新失败");
    }

    @Operation(summary = "同步到ERP")
    @PostMapping("/{id}/sync-erp")
    public Result<Void> syncToERP(@PathVariable Long id) {
        boolean success = orderService.syncToERP(id);
        return success ? Result.<Void>success("ERP同步成功", null) : Result.fail("同步失败");
    }

    @Operation(summary = "获取订单统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> stats = orderService.getOrderStatistics();
        return Result.success(stats);
    }
}
