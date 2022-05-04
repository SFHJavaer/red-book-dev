package com.mapper;

import com.pojo.SeckillOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 秒杀订单表 Mapper 接口
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {

    Long selectOrderIdById(Long orderId);
}
