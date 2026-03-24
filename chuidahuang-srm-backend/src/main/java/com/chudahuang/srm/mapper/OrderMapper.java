package com.chudahuang.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chudahuang.srm.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表（带供应商信息）
     */
    IPage<Order> selectOrderPageWithSupplier(Page<Order> page, @Param("orderNo") String orderNo, @Param("supplierId") Long supplierId, @Param("status") String status);
}
