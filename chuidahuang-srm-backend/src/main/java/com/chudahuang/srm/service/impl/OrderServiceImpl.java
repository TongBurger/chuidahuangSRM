package com.chudahuang.srm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chudahuang.srm.dto.*;
import com.chudahuang.srm.entity.*;
import com.chudahuang.srm.mapper.*;
import com.chudahuang.srm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderChangeHistoryMapper changeHistoryMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<OrderDTO> pageOrders(Page<Order> page, String orderNo, Long supplierId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(orderNo), Order::getOrderNo, orderNo)
                .eq(supplierId != null, Order::getSupplierId, supplierId)
                .eq(StrUtil.isNotBlank(status), Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = page(page, wrapper);

        // 转换为DTO
        Page<OrderDTO> dtoPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderDTO> dtoList = orderPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public OrderDTO getOrderDetail(Long id) {
        Order order = getById(id);
        if (order == null) {
            return null;
        }
        return convertToDTO(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderDTO dto) {
        // 查询供应商信息
        Supplier supplier = supplierMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new RuntimeException("供应商不存在");
        }

        Order order = new Order();
        BeanUtil.copyProperties(dto, order);
        order.setSupplierName(supplier.getName());
        order.setStatus("待确认");

        // 生成订单编号
        order.setOrderNo(generateOrderNo());

        // 计算订单总金额
        BigDecimal totalAmount = dto.getItems().stream()
                .map(item -> item.getPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        save(order);

        // 保存订单明细
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                BeanUtil.copyProperties(itemDTO, item);
                item.setOrderId(order.getId());
                item.setAmount(itemDTO.getPrice().multiply(itemDTO.getQuantity()));
                orderItemMapper.insert(item);
            }
        }

        // 记录变更历史
        recordChangeHistory(order.getId(), 1, "创建订单", "初始订单创建", "系统");

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order updateOrder(Long id, OrderDTO dto) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 记录变更前的状态
        String beforeContent = BeanUtil.beanToMap(order).toString();

        BeanUtil.copyProperties(dto, order, "id", "orderNo");

        // 重新计算订单总金额
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            BigDecimal totalAmount = dto.getItems().stream()
                    .map(item -> item.getPrice().multiply(item.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(totalAmount);
        }

        updateById(order);

        // 更新订单明细
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (OrderItemDTO itemDTO : dto.getItems()) {
                OrderItem item = new OrderItem();
                BeanUtil.copyProperties(itemDTO, item);
                item.setOrderId(order.getId());
                item.setAmount(itemDTO.getPrice().multiply(itemDTO.getQuantity()));
                orderItemMapper.insert(item);
            }
        }

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long id) {
        // 删除订单明细
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        // 删除变更历史
        changeHistoryMapper.delete(new LambdaQueryWrapper<OrderChangeHistory>().eq(OrderChangeHistory::getOrderId, id));
        // 删除订单
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOrder(Long id, boolean confirm, String reason) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (confirm) {
            order.setStatus("生产中");
        } else {
            order.setStatus("已拒绝");
        }

        updateById(order);

        // 记录变更历史
        String changeType = confirm ? "确认订单" : "拒绝订单";
        String description = confirm ? "供应商确认订单，开始生产" : "供应商拒绝订单: " + reason;
        recordChangeHistory(id, getNextVersion(id), changeType, description, "供应商");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long id, String status) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        String oldStatus = order.getStatus();
        order.setStatus(status);
        updateById(order);

        // 记录变更历史
        recordChangeHistory(id, getNextVersion(id), "状态变更",
                "订单状态从" + oldStatus + "变更为" + status, "系统");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean syncToERP(Long id) {
        Order order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // TODO: 调用用友U8接口同步订单
        order.setErpSyncStatus("同步中");
        updateById(order);

        // 模拟同步成功
        order.setErpSyncStatus("已同步");
        order.setErpSyncTime(java.time.LocalDateTime.now());
        updateById(order);

        return true;
    }

    @Override
    public Map<String, Object> getOrderStatistics() {
        List<Order> allOrders = list();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allOrders.size());
        stats.put("pendingConfirm", allOrders.stream().filter(o -> "待确认".equals(o.getStatus())).count());
        stats.put("inProduction", allOrders.stream().filter(o -> "生产中".equals(o.getStatus())).count());
        stats.put("shipped", allOrders.stream().filter(o -> "已发货".equals(o.getStatus())).count());
        stats.put("completed", allOrders.stream().filter(o -> "已完成".equals(o.getStatus())).count());

        BigDecimal totalAmount = allOrders.stream()
                .map(Order::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalAmount", totalAmount);

        return stats;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        return "PO" + LocalDate.now().toString().replace("-", "") +
                String.format("%04d", System.currentTimeMillis() % 10000);
    }

    /**
     * 记录变更历史
     */
    private void recordChangeHistory(Long orderId, Integer version, String changeType, String description, String operator) {
        OrderChangeHistory history = new OrderChangeHistory();
        history.setOrderId(orderId);
        history.setVersion(version);
        history.setChangeDate(LocalDate.now());
        history.setChangeType(changeType);
        history.setDescription(description);
        history.setOperator(operator);
        changeHistoryMapper.insert(history);
    }

    /**
     * 获取下一个版本号
     */
    private Integer getNextVersion(Long orderId) {
        Long count = changeHistoryMapper.selectCount(
                new LambdaQueryWrapper<OrderChangeHistory>().eq(OrderChangeHistory::getOrderId, orderId)
        );
        return count.intValue() + 1;
    }

    /**
     * 转换为DTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        BeanUtil.copyProperties(order, dto);

        // 查询订单明细
        List<OrderItemDTO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
        ).stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            BeanUtil.copyProperties(item, itemDTO);
            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(items);

        // 查询变更历史
        List<OrderChangeHistoryDTO> histories = changeHistoryMapper.selectList(
                new LambdaQueryWrapper<OrderChangeHistory>().eq(OrderChangeHistory::getOrderId, order.getId())
                        .orderByAsc(OrderChangeHistory::getVersion)
        ).stream().map(history -> {
            OrderChangeHistoryDTO historyDTO = new OrderChangeHistoryDTO();
            BeanUtil.copyProperties(history, historyDTO);
            return historyDTO;
        }).collect(Collectors.toList());
        dto.setChangeHistory(histories);

        return dto;
    }
}
