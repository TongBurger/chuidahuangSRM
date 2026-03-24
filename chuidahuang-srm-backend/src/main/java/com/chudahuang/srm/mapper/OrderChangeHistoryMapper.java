package com.chudahuang.srm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chudahuang.srm.entity.OrderChangeHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单变更历史Mapper
 */
@Mapper
public interface OrderChangeHistoryMapper extends BaseMapper<OrderChangeHistory> {
}
