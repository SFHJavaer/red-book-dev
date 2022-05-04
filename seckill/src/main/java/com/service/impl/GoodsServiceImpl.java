package com.service.impl;

import com.pojo.Goods;
import com.mapper.GoodsMapper;
import com.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-26
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    /**
     * 获取商品列表
     * @return
     */
    @Override
    public List<GoodsVo> findGoodsVo() {
        System.out.println("正在查询商品列表。。。。");
        return goodsMapper.findGoodsVo();
    }
    /**
     * 根据商品id获取商品详情
     * @param goodsId
     * @return
     */
    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}