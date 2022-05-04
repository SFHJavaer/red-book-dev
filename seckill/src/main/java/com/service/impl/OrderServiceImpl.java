package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exception.GlobalException;
import com.mapper.OrderMapper;
import com.mapper.SeckillOrderMapper;
import com.pojo.Order;
import com.pojo.SeckillGoods;
import com.pojo.SeckillOrder;
import com.pojo.User;
import com.service.IGoodsService;
import com.service.IOrderService;
import com.service.ISeckillGoodsService;
import com.service.ISeckillOrderService;
import com.util.JsonUtil;
import com.util.MD5Util;
import com.util.UUIDUtil;
import com.vo.GoodsVo;
import com.vo.OrderDetailVo;
import com.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 1.0.0
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    /**
     * 订单详情
     * @param
     * @return
     */
    @Override
    public OrderDetailVo detail(Long id) {
        if (null == id) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Long orderId = seckillOrderMapper.selectOrderIdById(id);
        Order order = orderMapper.selectById(orderId);

        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());

        OrderDetailVo detail = new OrderDetailVo();
        detail.setGoodsVo(goodsVo);
        detail.setOrder(order);
        return detail;
    }
    /**
     * 秒杀
     * @param user
     * @param goods
     * @return
     */
    @Override
    @Transactional
    public Order seckill(User user, GoodsVo goods) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goods.getId()));
//        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);

        boolean seckillGoodsResult = seckillGoodsService.update(new
                UpdateWrapper<SeckillGoods>().set("stock_count",seckillGoods.getStockCount()).eq("id",
                seckillGoods.getId()).gt("stock_count", 0));

        if (seckillGoods.getStockCount() < 1) {
            //判断是否还有库存
            valueOperations.set("isStockEmpty:" + goods.getId(), "0");
            return null;
        }
        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goods.getId(), seckillOrder);
        return order;
    }
    /**
     * 验证请求地址
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user==null|| StringUtils.isEmpty(path)){
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" +
                user.getId() + ":" + goodsId);
        return path.equals(redisPath);
    }
    /**
     * 生成秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" +
                goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }
}