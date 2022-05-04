package com.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 秒杀信息
 *
 * @author: LC
 * @date 2022/3/8 6:38 下午
 * @ClassName: SeckillMessage
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {

    private User User;

    private Long goodsId;
}
