package com.imooc.controller;


import com.imooc.RabbitMQConfig;
import com.imooc.grace.result.GraceJSONResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RefreshScope//实现配置的自动更新
public class HelloController {
    @Autowired
    public RabbitTemplate rabbitTemplate;

    @GetMapping("hello")
    public Object hello(){
        return GraceJSONResult.ok("成功");
    }
    //RabbitMQ的消息也是通过Controller接收到请求，发送映射请求的，使用官方APi
    @GetMapping("produce")
    public Object produce() throws Exception {

        //send消息即发送方相当于生产者
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.send",
                "我发了一个消息~~");


        /**
         * 路由规则
         * route-key
         * display.
         *      display.a.b
         *      display.public.msg
         *      display.a.b.c
         *      * 代表一个占位符
         *
         *  display.#
         *      display.a.b
         *      display.a.b.c.d
         *      display.public.msg
         *      display.delete.msg.do
         *      # 代表多个占位符
         *
         */

        return GraceJSONResult.ok();
    }

    @GetMapping("produce2")
    public Object produce2() throws Exception {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.delete",
                "我删除了一个消息~~");

        return GraceJSONResult.ok();
    }
}
