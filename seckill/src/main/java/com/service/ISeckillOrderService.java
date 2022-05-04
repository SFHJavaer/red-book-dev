package com.service;

import com.pojo.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pojo.User;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀订单表 服务类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
@Service
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
