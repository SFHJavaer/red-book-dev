package com.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exception.GlobalException;
import com.mapper.UserMapper;
import com.pojo.User;
import com.service.IUserService;
import com.util.*;
import com.vo.Loginvo;
import com.vo.RespBean;
import com.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 更新密码
     *
     * @param userTicket
     * @param id
     * @param password
     * @return
     */
    @Override
    public RespBean updatePassword(String userTicket, Long id, String password) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.inputPassToDbPass(password, user.getSalt()));
        int result = userMapper.updateById(user);
        if (1 == result) {
            //删除Redis
            redisTemplate.delete("user:" + userTicket);
            return RespBean.success();
        }
        return RespBean.error(RespBeanEnum.PASSWORD_UPDATE_FAIL);
    }

    @Override
    public RespBean doLogin(HttpServletRequest request, HttpServletResponse response, Loginvo loginvo) {
        String mobile = loginvo.getMobile();
        String password = loginvo.getPassword();
//        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
//            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
//
//        }
//        //不管前端是否做手机号校验，后端最后一条防线一定要做校验
//        if(!ValidatorUtil.isMobile(mobile)){
//            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
//        }
        //根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if(user==null){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //校验密码
        if
        (!MD5Util.formPassToDBPass(password,user.getSalt()).equals(user.getPassword())){
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }
        //生成cookie
        String ticket = UUIDUtil.uuid();

        redisTemplate.opsForValue().set("user:" + ticket, user);//JsonUtil.object2JsonStr(user)
        CookieUtil.setCookie(request, response, "userTicket", ticket);


        return RespBean.success(ticket);
//        //把user对象存入session，读取时再强转为user类型,不再使用下面的session存储数据，直接保存到redis中
//        request.getSession().setAttribute(ticket,user);
//        CookieUtil.setCookie(request,response,"userTicket",ticket);
//        return RespBean.success(ticket);


    }
    //由cookie获取user
    @Override
    public User getByUserTicket(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
//        String userJson = (String) redisTemplate.opsForValue().get("user:" + userTicket);
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
//        User user = JsonUtil.jsonStr2Object(userJson, User.class);
        if (null != user) {
            CookieUtil.setCookie(request,response,"userTicket",userTicket);
        }

        return user;
    }
}
