package com.imooc.interceptor;


import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.base.BaseInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {
    //通过请求头来校验修改资料权限
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {


        // 从header中获得用户id和token
        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");

        // 判断header中用户id和token不能为空，根据config的配置可以知道是用于在用户相关图片修改以及基础信息的修改（密码不算基础信息）
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userToken)) {
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(redisToken)) {
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            } else {
                // 比较token是否一致，如果不一致，表示用户在别的手机端登录
                if (!redisToken.equalsIgnoreCase(userToken)) {
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        } else {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
            return false;
        }

        /**
         * true: 请求放行
         * false: 请求拦截
         */
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
