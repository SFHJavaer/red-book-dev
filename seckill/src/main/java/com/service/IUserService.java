package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pojo.User;
import com.vo.Loginvo;
import com.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 登录Service实现
 * 用户表 服务类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-25
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(HttpServletRequest request, HttpServletResponse response, Loginvo loginvo);
    /**

    * Description:根据cookie获取用户，后续存入到redis中

    * date: 2022/3/26 15:48

    * @author: sfh

    * @since JDK 1.8

    */
    User getByUserTicket(String userTicket,HttpServletRequest request,HttpServletResponse response);
    /**
            * 更新密码
* @param userTicket
* @param id
* @param password
* @return
        */
    RespBean updatePassword(String userTicket,Long id,String password);
}
