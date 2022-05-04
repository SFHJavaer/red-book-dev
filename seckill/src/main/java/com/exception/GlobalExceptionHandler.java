package com.exception;

import com.vo.RespBean;
import com.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/**
 * 全局异常处理:service抛出来的异常被Controller接收，上层谁调用，出了异常就抛给谁
 *
 * @author zhoubin
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e) {
        //如果指向父类Exception的e对象属于子类异常，但不是子类异常对象，可以进行强转
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return RespBean.error(ex.getRespBeanEnum());
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常：" +
                    ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        return RespBean.error(RespBeanEnum.ERROR);
    }
}
//强转出错的情况：
//1.两个类没有继承关系
//2.父类强转为子类时出现错误：使用instanceof运算符提前判断