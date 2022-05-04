package com.vo;


import com.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品详情返回对象
 *
 * @author: LC
 * @date 2022/3/6 10:06 上午
 * @ClassName: DetailVo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailVo {


    private User tUser;

    private GoodsVo goodsVo;

    private int secKillStatus;

    private int remainSeconds;


}