package com.imooc.service.impl;

import com.imooc.enums.MessageEnum;
import com.imooc.mo.MessageMO;
import com.imooc.pojo.Users;
import com.imooc.repository.MessageRepository;
import com.imooc.service.MsgService;
import com.imooc.service.UserService;
import com.imooc.base.BaseInfoProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserService userService;
    @Override
    public void createMsg(String fromUserId, String toUserId, Integer type, Map msgContent) {
        //根据传入的user进行获取它的特征
        //Map：msgContent保存了多个特征映射，消息会展示所需要的视频封面和视频，将Controller设置的map赋值给MO的Content属性
        //传入的是map类型即{key=value}，而json是{"key":"value"}形式
        Users fromUser = userService.getUser(fromUserId);
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(fromUserId);
        messageMO.setFromNickname(fromUser.getNickname());
        messageMO.setFromFace(fromUser.getFace());

        messageMO.setToUserId(toUserId);

        messageMO.setMsgType(type);
        if (msgContent != null) {
            messageMO.setMsgContent(msgContent);
        }

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }
/**
 * @description: 消息模块的查询消息业务
 * @param: toUserId
page
pageSize
 * @return: java.util.List<com.imooc.mo.MessageMO>
 * @author Administrator
 * @date: 2022/8/11 14:17
 */
    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        /***
         * 这里查询列表也可以直接使用Repository提供的findAll方法，参数传入的是Example对象进行条件查询
         * 这里的PageRequest的目的是创建一个Pageable对象，of方法有多个重载方法,可以选择排序方式和具体的排序字段
         */

        Pageable pageable = PageRequest.of(page, pageSize,Sort.Direction.DESC,"createTime");
        List<MessageMO> list= messageRepository.findAllByToUserIdEqualsOrderByCreateTimeDesc(toUserId, pageable);
        /***
         * 消息列表也除了头像和封面等附加信息（Map），如果是关注消息，还需要判断我是不是关注了对方，如果是的话按钮要变成互关
         * 直接将isFriend赋值set给对象都返回即可，前端会接收msgContent进行相应的处理（字段必须对应）
         */
        for (MessageMO msg : list) {
            // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
            if (msg.getMsgType() != null && msg.getMsgType() == MessageEnum.FOLLOW_YOU.type) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }

                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());

                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
            }
        }
        return list;
    }
}
