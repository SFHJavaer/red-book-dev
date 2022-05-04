package com.exception;


import com.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 全局异常
 *
 * @author zhoubin
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//作为异常类别忘了继承RuntimeException
public class GlobalException extends RuntimeException {
    private RespBeanEnum respBeanEnum;
}