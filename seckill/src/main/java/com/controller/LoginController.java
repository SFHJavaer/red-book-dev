package com.controller;

import com.service.IUserService;
import com.vo.Loginvo;
import com.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**

* Description:登录Controller

* date: 2022/3/25 19:14

* @author: sfh

* @since JDK 1.8

*/
@Controller
//重点：用@RestController默认给所有方法加respbody，会在本页面返回对象而不是做页面跳转
@RequestMapping("/login")
@Slf4j
public class LoginController {
    @Autowired
    private IUserService iUserService;
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }
    @RequestMapping("/doLogin")
    @ResponseBody//结果返回到请求体中
    //在Controller直接加HTTPservletRequest获取请求，传给下面的service层
    public RespBean doLogin(HttpServletRequest request, HttpServletResponse response, @Valid Loginvo loginvo){
//        log.info(loginvo.toString());
        //登录逻辑在service服务实现
        return iUserService.doLogin(request,response,loginvo);
    }
}
