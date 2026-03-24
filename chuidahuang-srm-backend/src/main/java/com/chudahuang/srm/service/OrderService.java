package com.chudahuang.srm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chudahuang.srm.dto.OrderDTO;
import com.chudahuang.srm.entity.Order;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单列表
     */
    Page<OrderDTO> pageOrders(Page<Order> page, String orderNo, Long supplierId, String status);

    /**
     * 获取订单详情
     */
    OrderDTO getOrderDetail(Long id);

    /**
     * 创建订单
     */
    Order createOrder(OrderDTO orderDTO);

    /**
     * 更新订单
     */
    Order updateOrder(Long id, OrderDTO orderDTO);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);

    /**
     * 供应商确认订单
     */
    boolean confirmOrder(Long id, boolean confirm, String reason);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long id, String status);

    /**
     * 同步到ERP (用友U8)
     */
    boolean syncToERP(Long id);

    /**
     * 获取订单统计信息
     */
    java.util.Map<String, Object> getOrderStatistics();
}
