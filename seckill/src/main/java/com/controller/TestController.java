package com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * 测试
 *
 * @author zhoubin
 * @since 1.0.0
 */
@Controller
@RequestMapping("/demo")
public class TestController {
    /**
     * 测试页面跳转
     *
     * @return
     */
    @RequestMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("name", "xxxx");
        return "hello";
    }
}