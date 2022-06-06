package com.imooc.controller;

import com.imooc.MinIOConfig;
import com.imooc.bo.UpdatedUserBO;
import com.imooc.enums.FileTypeEnum;
import com.imooc.enums.UserInfoModifyType;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.MinIOUtils;
import com.imooc.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口模块")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    private UserService userService;
    @Autowired
    private MinIOConfig minIOConfig;

    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception {

        Users user = userService.getUser(userId);
        //规范上要使用前端交互vo类，所以后端的Users类给new UsersVO进行赋值
        //赋值完基本用户信息还不够，usersvo的属性数量要大于users（pojo），所以还要set剩余展示属性
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 我的关注博主总数量，频繁更新和展示的数据都放在redis中，把redis当作快速数据库
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数，视频博主（点赞/喜欢）总和
//        String likedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String likedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);
        //五个变量用来统计数量
        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likedVlogCounts = 0;
        Integer likedVlogerCounts = 0;
        Integer totalLikeMeCounts = 0;
        //加上条件进行判断，如果redis获取的数据不是null，那么就转换为数字
        if (StringUtils.isNotBlank(myFollowsCountsStr)) {
            myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        }
        if (StringUtils.isNotBlank(myFansCountsStr)) {
            myFansCounts = Integer.valueOf(myFansCountsStr);
        }
//        if (StringUtils.isNotBlank(likedVlogCountsStr)) {
//            likedVlogCounts = Integer.valueOf(likedVlogCountsStr);
//        }
        if (StringUtils.isNotBlank(likedVlogerCountsStr)) {
            likedVlogerCounts = Integer.valueOf(likedVlogerCountsStr);
        }

        totalLikeMeCounts = likedVlogCounts + likedVlogerCounts;
        //最后将计算出的数赋值给VO对象，vo对象要交给前端进行展示
        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(totalLikeMeCounts);
        //直接ok，携带着Object返回即可
        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdatedUserBO updatedUserBO,
                                          @RequestParam Integer type)
            throws Exception {

        UserInfoModifyType.checkUserInfoTypeIsRight(type);
        //传入校验方法而不是直接update，由双参反调
        Users newUserInfo = userService.updateUserInfo(updatedUserBO, type);
        //因为更新用户信息时，每一次只更新一项，其他项均为null，mapper执行的方法null项不会修改，所以不需要使用VO，直接返回user进行展示即可
        //前端是需要一个user对象来进行数据刷新的，一般当VO与所存在实体类有属性差异时新创建vo类
        return GraceJSONResult.ok(newUserInfo);
    }



    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception {

        if (type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        String fileName = file.getOriginalFilename();

        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                fileName,
                file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + fileName;


        // 修改图片地址到数据库
        UpdatedUserBO updatedUserBO = new UpdatedUserBO();
        updatedUserBO.setId(userId);

        if (type == FileTypeEnum.BGIMG.type) {
            updatedUserBO.setBgImg(imgUrl);
        } else {
            updatedUserBO.setFace(imgUrl);
        }
        //设置了哪些条件就会按照条件进行匹配并更新
        Users users = userService.updateUserInfo(updatedUserBO);

        return GraceJSONResult.ok(users);
    }
}
