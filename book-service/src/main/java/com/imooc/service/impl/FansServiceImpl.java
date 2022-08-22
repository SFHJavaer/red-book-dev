package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.base.RabbitMQConfig;
import com.imooc.enums.MessageEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.FansMapper;
import com.imooc.mapper.FansMapperCustom;
import com.imooc.mo.MessageMO;
import com.imooc.pojo.Fans;
import com.imooc.service.FansService;
import com.imooc.service.MsgService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import com.imooc.vo.FansVO;
import com.imooc.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;
    //使用sid生成全局的唯一id
    @Autowired
    private Sid sid;
    @Autowired
    private RedisOperator redisOperator;
    @Autowired
    private FansMapperCustom fansMapperCustom;
    //解耦之后，不直接调msgService去MongoDB存储消息了，而是先让MQ转个手，用队列去消息入库，用MQ的队列还是因为功能多且方便
    @Autowired
    private MsgService msgService;
    @Autowired
    //服务层调Template来发送消息，一个队列是一个路由键的通配，所以使用topic模式，这个队列再根据具体key去调这个固定的service，一个queue对应一个服务
    //通配符决定队列也就决定了处理服务，具体的key决定了执行该服务中的哪些方法（手写if判断）
    private RabbitTemplate rabbitTemplate;
    @Override
    public void doFollow(String myId, String vlogerId) {
        String fid = sid.nextShort();
        Fans fans =new Fans();
        fans.setId(fid);
        //vloger的粉丝id设置为当前用户id
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);

        // 判断对方是否关注我，如果关注我，那么双方都要互为朋友关系
        Fans vloger = queryFansRelationship(vlogerId, myId);
        if (vloger != null) {
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);

            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            //如果volger已经关注了user的话，vloger的isfriend更新为1
            fansMapper.updateByPrimaryKeySelective(vloger);
        } else {
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        //当前粉丝插入（userid作为主键），所以两个用户之间的的关注关系，是以其主键分别的两条记录，代表了当前的用户的对vloger即另一个用户的关注情况
        fansMapper.insert(fans);
        /***
         * 系统消息：关注，消息模块是队列中”消息“的生产者
         * 注意如果某个field为null，那么插入到NOSQL中即mongodb中就不会显示该字段，这是nosql的特性
         * 所以通过一个collection集合，里面的数据是弱关系型的，里面的数据字段不一定是统一的
          */
//        msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(myId);
        messageMO.setToUserId(vlogerId);
        // 优化：使用mq异步解耦,交换机、路由键、对象
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg." + MessageEnum.FOLLOW_YOU.enValue,
                //方法传入的对象是Object类型，但是在接受内容的时候是String类型的payload，所以要将对象转换为json类型
                JsonUtils.objectToJson(messageMO));

    }
    /**

    * Description:工具方法，查询当前要关注的创作者是否已经关注了本id，如关注了后面要设置朋友关系

    * date: 2022/5/10 11:01

    * @author: sfh

    * @since JDK 1.8

    */
    public Fans queryFansRelationship(String fanId, String vlogerId) {
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", vlogerId);
        criteria.andEqualTo("fanId", fanId);

        List list =  fansMapper.selectByExample(example);

        Fans fan = null;
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            fan = (Fans)list.get(0);
        }

        return fan;
    }
    /**

    * Description:取关

    * date: 2022/5/10 11:52

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public void doCancel(String myId, String vlogerId) {
        // 判断我们是否朋友关系，如果是，则需要取消双方的关系
        Fans fan = queryFansRelationship(myId, vlogerId);
        //如果存在朋友关系
        if (fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type) {
            // 抹除双方的朋友关系，自己的关系删除即可
            Fans pendingFan = queryFansRelationship(vlogerId, myId);
            pendingFan.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(pendingFan);
        }

        // 删除自己的关注关联表记录
        fansMapper.delete(fan);

    }

    @Override
    public boolean queryDoIFollowVloger(String myId, String vlogerId) {
        /***
         * 是否为 null
         * 是否为 ""
         * 是否为空字符串(引号中间有空格)  如： "     "。
         * 　StringUtils的isBlank()方法可以一次性校验这三种情况，返回值都是true。
         */
        String followVloger = redisOperator.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);
        if(!StringUtils.isBlank(followVloger)){
            return true;
        }

        return false;
    }

    @Override
    public PagedGridResult queryMyFollows(String myId, Integer page, Integer pageSize) {
        /***
         * 在业务实现类中进行分页，数据层仅执行查询操作，利用pagehelper底层的拦截器对结果集进行分页
         */
        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<VlogerVO> list = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(list, page);

    }

    @Override
    public PagedGridResult queryMyFans(String myId, Integer page, Integer pageSize) {
        /**
         * <判断粉丝是否是我的朋友（互粉互关）>
         * 普通做法：
         * 多表关联+嵌套关联查询，这样会违反多表关联的规范，不可取，高并发下回出现性能问题
         *
         * 常规做法：
         * 1. 避免过多的表关联查询，先查询我的粉丝列表，获得fansList
         * 2. 判断粉丝关注我，并且我也关注粉丝 -> 循环fansList，获得每一个粉丝，再去数据库查询我是否关注他
         * 3. 如果我也关注他（粉丝），说明，我俩互为朋友关系（互关互粉），则标记flag为true，否则false
         *
         * 高端做法：
         * 1. 关注/取关的时候，关联关系保存在redis中，不要依赖数据库
         * 2. 数据库查询后，直接循环查询redis，避免第二次循环查询数据库的尴尬局面
         * 所以使用redis的条件，不光是频繁访问的数据作缓存，对于查询复杂的关系问题（可能不频繁），查询出的关系不包括具体的DB数据，那么完全可以使用redis存储
         */


        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        PageHelper.startPage(page, pageSize);

        List<FansVO> list = fansMapperCustom.queryMyFans(map);
        /***
         * 实现互粉标记，如果redis查询到myid用户有关注我的粉丝列表中的粉丝的记录，那么前端就会显示互粉说明是friend，而不是回粉
         */
        for (FansVO f : list) {
            String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + f.getFanId());
            if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                f.setFriend(true);
            }
        }

        return setterPagedGrid(list, page);
    }
}
