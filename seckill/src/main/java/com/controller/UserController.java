package com.controller;


import com.pojo.User;
import com.vo.RespBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author sunfuhao
 * @since 2022-03-25
 */
@Controller
@RequestMapping("/user")
public class UserController {
    /**
     * 用户信息(测试)
     * @param user
     * @return
     */
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        return RespBean.success(user);
    }
}

