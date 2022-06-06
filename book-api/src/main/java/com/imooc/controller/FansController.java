package com.imooc.controller;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.service.FansService;
import com.imooc.service.UserService;
import com.imooc.base.BaseInfoProperties;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "FansController 粉丝相关业务功能的接口")
@RequestMapping("fans")
@RestController
public class FansController extends BaseInfoProperties {

    @Autowired
    private UserService userService;
    @Autowired
    private FansService fansService;
    /**

    * Description:关注请求

    * date: 2022/5/10 11:50

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId) {

        // 判断两个id不能为空
        if (StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // 判断当前用户，自己不能关注自己（看业务逻辑）
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        // 判断两个id对应的用户是否存在
        Users vloger = userService.getUser(vlogerId);
        Users myInfo = userService.getUser(myId);

        // fixme: 两个用户id的数据库查询后的判断，是分开好？还是合并判断好？分开，减少对db的访问
        if (myInfo == null || vloger == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);
        }

        // 保存粉丝关系到数据库
        fansService.doFollow(myId, vlogerId);

        // 博主的粉丝+1，我的关注+1，统计数量使用缓存
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        // 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈，value随意设置，只要有值就代表存在关注关系
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");

        return GraceJSONResult.ok();
    }
    /**

    * Description:取关请求

    * date: 2022/5/10 11:53

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId) {

        // 删除业务的执行
        fansService.doCancel(myId, vlogerId);

        // 博主的粉丝-1，我的关注-1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        // 我和博主的关联关系，依赖redis，不要存储数据库，避免db的性能瓶颈
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);

        return GraceJSONResult.ok();
    }
    /**

    * Description:查询我是否关注了该博主，如果关注了那么就将关注按钮变为已关注

    * date:  12:02

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId) {
        return GraceJSONResult.ok(fansService.queryDoIFollowVloger(myId, vlogerId));
    }
    /**

    * Description:查询我的关注列表，前端默认列表都是分页的

    * date: 2022/5/10 18:37

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fansService.queryMyFollows(
                        myId,
                        page,
                        pageSize));
    }
    /**

    * Description:查询我的粉丝列表

    * date: 2022/5/10 18:38

    * @author: sfh

    * @since JDK 1.8

    */

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {
        return GraceJSONResult.ok(
                fansService.queryMyFans(
                        myId,
                        page,
                        pageSize));
    }

}
