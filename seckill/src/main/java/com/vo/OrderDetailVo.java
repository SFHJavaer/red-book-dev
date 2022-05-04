package com.vo;
import com.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {
    /**
     * @author zhoubin
     * @since 1.0.0
     */
        private Order order;
        private GoodsVo goodsVo;
}
