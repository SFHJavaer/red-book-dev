package com.imooc;

import com.imooc.base.RabbitMQConfig;
import com.imooc.enums.MessageEnum;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mo.MessageMO;
import com.imooc.service.MsgService;
import com.imooc.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**

* Description:自定义的消费者类（监听器类），一般生产者和消费者都是分布式的，在不同的服务器上

* date: 2022/5/12 15:35

* @author: sfh

* @since JDK 1.8

*/
@Slf4j
@Component
public class RabbitMQConsumer {
    @Autowired
    private MsgService msgService;
    /***
     * 通过对方法添加注解，将方法变为监听方法，注意要指定具体的监听队列，Queues可以是多个
     * 一个消息只能被消费一次，并且我们可以在消费者一方去获取消息的各种参数getMessageProperties()，比如路由key
     * 可以在消费即具体业务处理时通过routingkey判断业务类别，来执行具体的何种业务处理逻辑
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_SYS_MSG)
    //自定义方法，参数是payLoad和message，前者是消息内容（载体），后者是消息(org.springframework.amqp.core.Message)

    public void wahtchQueue(String payload, Message message){
        log.info(payload);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info(routingKey);
        /***
         * 消息MQ的作用就是把原来服务层的业务处理和消息发送代码进行解耦，只需要发送一个消息给MQ
         * 消费者监听去进行消息的非业务处理（通过判断路由键）
         * 将具体的msgService内容迁移到了consunmer中
         * 如何获取对象：通过将json的payload进行反序列化转换为对象
         *
         */
        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);
        if(routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)){
            //关注不需要展示附加信息，MO中的信息足够，所以将content设置为null
            msgService.createMsg(messageMO.getFromUserId(), messageMO.getToUserId(), MessageEnum.FOLLOW_YOU.type, null);

        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.COMMENT_VLOG.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.REPLY_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_COMMENT.type,
                    messageMO.getMsgContent());
        } else {
            //如果routingKey没有匹配，那么该消息异常
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

    }
}
