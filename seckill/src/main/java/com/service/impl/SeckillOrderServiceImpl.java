package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mapper.SeckillOrderMapper;
import com.pojo.SeckillOrder;
import com.pojo.User;
import com.service.ISeckillOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀订单表 服务实现类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Long getResult(User user, Long goodsId) {
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new
                QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id",
                goodsId));
        if (null != seckillOrder) {
            return seckillOrder.getId();
        } else {
            if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
                return -1L;
            }else {
                return 0L;
            }
        }
    }

}
