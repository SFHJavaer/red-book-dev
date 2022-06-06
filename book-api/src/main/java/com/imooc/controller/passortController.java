package com.imooc.controller;

import com.imooc.bo.RegistLoginBO;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.IPUtil;
import com.imooc.utils.SMSUtils;
import com.imooc.vo.UsersVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.UUID;

//类上加文档doc接口的注解，方法上也可以加@Api，用属性指定
@Api(tags = "PassportController 通信证接口模块")
@RequestMapping("passport")
@RestController
public class passortController extends BaseInfoProperties {
    @Autowired
    private SMSUtils smsUtils;
    @Autowired
    private UserService userService;

    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,HttpServletRequest request){

        //如果手机号为空则直接返回，不发送短信
        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.errorMsg("手机号为空");
        }
        //从request请求中获取ip地址，根据ip地址进行限制60s发送短信，而不是在redis设置60秒失效时间，实现效果是相同的
        //腾讯云在发送频率上也有限制
        String userIp = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE+":"+userIp, userIp);
        //获得6位的随机验证码
        String code = (int)((Math.random() *9 +1)*100000)+"";
        try {
            smsUtils.sendSMS(mobile, code);
            //发送完之后将验证码存入redis进行验证
            redis.set(MOBILE_SMSCODE+":"+mobile, code,30*60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return GraceJSONResult.ok();
    }
    /**

    * Description:登录请求

    * date: 2022/5/7 17:10

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("login")
    public GraceJSONResult login(@Valid @RequestBody RegistLoginBO registLoginBO
    //BindingResult result方法参数每一个Controller都要加，造成了方法参数的侵入性
    ){
        /***
         * BindingResult是habilete valiation框架提供的绑定结果集
         * 如果包含了erroe，就返回前端，用hasxxx方法
         */
//        if(result.hasErrors()){
//            Map<String, String> map = getErrors(result);
//            GraceJSONResult.errorMap(map);
//        }
        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();
        //1、从redis获取验证码
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        //为空（验证码会失效）或者不相等就返回错误
        if(StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)){
            return  GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_RESPONSE_NO_INFO);

        }
        //2、验证手机号成功后，判断用户是否存在再决定是注册还是登录,查持久化用户数据库，当然不是缓存（临时）
        Users user = userService.queryMobileIsExist(mobile);
        //判断是否存在
        if(user == null){
            //null创建用户
            user = userService.createUser(mobile);
        }//不为空可以进行判断其他用户状态，比如冻结等等，按用户需求
        //3、不为空分布式会话（session）
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN+":"+user.getId(), uToken);
        //4、登陆成功后删除redis的60s验证码，注意验证码只能使用一次
        redis.del(MOBILE_SMSCODE+""+mobile);
        //5、返回含token的用户信息（前端要保存和使用（用于它的本地会话））
        //使用视图实体类vo进行返回，而不是在pojo中对应pojo增加属性，多建一个专属的类，各司其职，返回token属性的实体时选择vo类而不是业务层pojo，Controller中进行了新属性的添加或设置
        UsersVO usersVO = new UsersVO();
        //使用对象（同名）属性批量复制工具类
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);//手动设置下token属性，因为uToken刚生成的
        return GraceJSONResult.ok(usersVO);
    }
    /**

    * Description:退出操作

    * date: 2022/5/7 17:10

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception {

        // 后端只需要清除用户的token信息即可，前端也需要清除，清除本地app中的用户信息和token会话信息
        redis.del(REDIS_USER_TOKEN + ":" + userId);

        return GraceJSONResult.ok();
    }

}
