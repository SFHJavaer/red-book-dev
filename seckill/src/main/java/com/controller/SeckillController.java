package com.controller;

/**
* <p>
* 前端控制器
* </p>
*
* @author zhoubin
* @since 1.0.0
*/
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pojo.Order;
import com.pojo.SeckillMessage;
import com.pojo.SeckillOrder;
import com.pojo.User;
import com.rabbitmq.MQSender;
import com.service.IGoodsService;
import com.service.IOrderService;
import com.service.ISeckillOrderService;
import com.util.JsonUtil;
import com.vo.GoodsVo;
import com.vo.RespBean;
import com.vo.RespBeanEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
//    @Autowired
//    private DefaultRedisScript<Long> script;


    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
//        boolean check = orderService.checkPath(user, goodsId, path);
//        if (!check) {
//            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
//        }
        //判断统一用户是否重抢
        String seckillOrderJson = (String)
                redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(seckillOrderJson)) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //内存标记
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        //预减库存
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if (stock < 0) {
            EmptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
//       //预减库存
//       Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
//       if (stock < 0) {
//           EmptyStockMap.put(goodsId,true);
//           return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//       }
//        Long stock = (Long) redisTemplate.execute(script,
//                Collections.singletonList("seckillGoods:" + goodsId),
//                Collections.EMPTY_LIST);
//        if (stock <= 0) {
//            EmptyStockMap.put(goodsId, true);
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
        // 请求入队，立即返回排队中
        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(message));
        //返回0交给前端进行判断
        return RespBean.success(0);
    }

    /**
     * 获取秒杀结果
     *
     * @param user
     * @param goodsId
     * @return orderId:成功，-1：秒杀失败，0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 获取秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);


            /**
             * Description:初始化指定的方法
             * date: 2022/3/29 16:01
             * @author: sfh

             * @since JDK 1.8

             */
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(),
                    goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }


}
