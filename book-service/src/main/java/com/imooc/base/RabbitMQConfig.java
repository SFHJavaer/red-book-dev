package com.imooc.base;//package com.imooc;//package com.imooc;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * 根据模型编写代码：
     * 1. 定义交换机
     * 2. 定义队列
     * 3. 创建交换机
     * 4. 创建队列
     * 5. 队列和交换机的绑定
     */

    public static final String EXCHANGE_MSG = "exchange_msg";

    public static final String QUEUE_SYS_MSG = "queue_sys_msg";
    //记住绑定两个参数到交换机，Exchange类型，是否持久化、最后使用ExchangeBuilder的build方法进行创建
    @Bean(EXCHANGE_MSG)
    public Exchange exchange() {
        return ExchangeBuilder                      // 构建交换机
                .topicExchange(EXCHANGE_MSG)        // 使用topic类型，参考：https://www.rabbitmq.com/getstarted.html
                .durable(true)                      // 设置持久化，重启mq后依然存在
                .build();
    }
    //可以直接将Bean名称写入字符串
    @Bean(QUEUE_SYS_MSG)
    public Queue queue() {
        //new 一个普通Queue即可
        return new Queue(QUEUE_SYS_MSG);
    }

    @Bean
    //@Qualifier按名称注入Bean，因为交换机和队列可能有很多（重复的类型）
    public Binding binding(@Qualifier(EXCHANGE_MSG) Exchange exchange,
                           @Qualifier(QUEUE_SYS_MSG) Queue queue) {
        /***
         * routingKey是在绑定交换机和队列时就进行设置的，当在具体的convertAndSend发送消息时
         * 根据具体消息的routingKey来查找对应的交换机队列
         */


        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("sys.msg.*")          // 定义路由规则（requestMapping），规则才能使用*和#
                .noargs();

        // FIXME: * 和 # 分别代表什么意思？
        /***
         * *在cron表达式中*代表一个任意的字符，占一个位
         * 而在routingkey规则中，一个*代表两个分隔符(.)之间的任意占位，而#代表任意多个分隔符和占位符号
         */
    }


}
