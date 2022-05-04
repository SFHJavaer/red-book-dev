package com.mapper;

import com.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();
    /**
     * 根据商品id获取商品详情
     * @param goodsId
     * @return
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
