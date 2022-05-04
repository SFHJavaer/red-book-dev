package com.service;

import com.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
public interface IGoodsService extends IService<Goods> {
    List<GoodsVo> findGoodsVo();
    /**
     * 根据商品id获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
