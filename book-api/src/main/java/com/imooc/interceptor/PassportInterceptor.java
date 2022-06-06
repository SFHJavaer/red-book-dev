package com.imooc.interceptor;

import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    //ctrl+i快捷键进行接口方法的实现

    @Override
    //访问Controller之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获得ip
        String userIp = IPUtil.getRequestIp(request);
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);
        log.info("IP地址为"+userIp);
        if(keyIsExist){
            //不直接throws异常，而是在具体的异常类中进行抛出，有专门的类进行处理，解耦合
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            log.info("短信发送频率过大");
            return false;
        }
        /**
         *拦截器Boolean
         * false代表请求拦截
         *true代表请求放行
        */
        return true;
    }

    @Override
    //访问之后，渲染视图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    //渲染完之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
