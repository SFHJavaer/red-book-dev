package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pojo.Order;
import com.pojo.User;
import com.vo.GoodsVo;
import com.vo.OrderDetailVo;

/**
 * <p>
 * 服务类
 * </p>
 * @author zhoubin
 * @since 1.0.0
 */
public interface IOrderService extends IService<Order> {
    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    Order seckill(User user, GoodsVo goods);
    /**
            * 订单详情
* @param orderId
* @return
        */
    OrderDetailVo detail(Long orderId);
    /**
     * 验证秒杀地址
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    boolean checkPath(User user, Long goodsId, String path);
    /**
     * 生成秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(User user, Long goodsId);
}
