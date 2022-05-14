package com.imooc.controller;

import com.imooc.bo.VlogBO;
import com.imooc.enums.YesOrNo;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.service.VlogService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "VlogController 短视频相关业务功能的接口")
@RequestMapping("vlog")
@RestController
@RefreshScope
public class VlogController extends BaseInfoProperties {

    @Autowired
    private VlogService vlogService;

    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO) {
        // FIXME 作业，校验VlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();
    }
    @GetMapping("indexList")
    //注意search这里默认值可为0
    public GraceJSONResult indexList(@RequestParam String userId,@RequestParam(defaultValue = "" ) String search
    ,@RequestParam Integer page,@RequestParam Integer pageSize) {
        //为空为默认情况
        if(page == null){
            page  = COMMON_START_PAGE;
        }
        if(pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult pagedGridResult = vlogService.getIndexVlogList(userId,search, page, pageSize);


        return GraceJSONResult.ok(pagedGridResult);
    }
    //请求类型不要弄错了，即便是请求视频，也是需要接收参数去请求的，只要携带着参数就是post请求
    //在用户页面请求视频详情的前端有问题
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam(defaultValue = "") String userId,
                                  @RequestParam String vlogId) {
        System.out.println("-------------------------");
        System.out.println(vlogId);

        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }
    /**

    * Description:将作品设置为private，public有另外的一个单独的方法

    * date: 2022/5/9 20:13

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.YES.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }
    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                           @RequestParam String vlogId) {
        vlogService.changeToPrivateOrPublic(userId,vlogId,YesOrNo.NO.type);
        return GraceJSONResult.ok(vlogService.getVlogDetailById(userId, vlogId));
    }
    /**

    * Description:获取我的公开作品列表，和下面的private可以合并
     * 但是由于前端给定了请求形式，这样可以降低耦合，一个作品类型一个方法
     * 如果需要降低代码量的话，可以在前端将链接传入一个type，在后端进行判断，或者改为Restful风格的，直接将public/private传入进行pathvaiable的判断

    * date: 2022/5/9 20:30

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.NO.type);
        return GraceJSONResult.ok(gridResult);
    }
    /**

    * Description:我的私密作品列表

    * date: 2022/5/9 20:32

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.queryMyVlogList(userId,
                page,
                pageSize,
                YesOrNo.YES.type);
        return GraceJSONResult.ok(gridResult);
    }
    /**

    * Description:我的点赞视频列表

    * date: 2022/5/10 21:42

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyLikedVlogList(userId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
    /**

    * Description:点赞视频请求

    * date: 2022/5/10 20:09

    * @author: sfh

    * @since JDK 1.8

    */
    //@Value读取配置文件中的值（根据规定格式去匹配nacos的动态配置文件）
    @Value("${nacos.counts}")
    private Integer nacosCounts;
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogerId,
                                @RequestParam String vlogId) {
        // 我点赞的视频，关联关系保存到数据库,持久化存储
        vlogService.userLikeVlog(userId, vlogerId,vlogId);

        // 点赞完毕，获得当前在redis中的总数
        // 比如获得总计数为 1k/1w/10w，假定阈值（配置）为2000
        // 此时1k满足2000，则触发入库
        //question：出发之后如何继续增加阈值，不然达标后会一直存入数据库
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("======" + REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId + "======");
        Integer counts = 0;
        if (StringUtils.isNotBlank(countsStr)) {
            counts = Integer.valueOf(countsStr);
            if (counts >= nacosCounts) {
                vlogService.flushCounts(vlogId, counts);
            }
        }
        return GraceJSONResult.ok();
    }
    /**

     * Description:取消点赞视频请求

     * date: 2022/5/10 20:09

     * @author: sfh

     * @since JDK 1.8

     */
    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String vlogerId,
                                  @RequestParam String vlogId) {
        // 我取消点赞的视频，关联关系删除
        vlogService.userUnLikeVlog(userId, vlogerId,vlogId);

        return GraceJSONResult.ok();
    }
    /**

    * Description:查询要访问视频的实时点赞数量

    * date: 2022/5/10 21:24

    * @author: sfh

    * @since JDK 1.8

    */
    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId) {
        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }
    /**

    * Description:查询我关注博主的视频，三表关联查询

    * date: 2022/5/11 10:08

    * @author: sfh

    * @since JDK 1.8

    */
    @GetMapping("followList")
    public GraceJSONResult followList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFollowVlogList(myId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }
    /**

     * Description:查询我朋友的视频，三表关联查询

     * date: 2022/5/11 10:08

     * @author: sfh

     * @since JDK 1.8

     */

    @GetMapping("friendList")
    public GraceJSONResult friendList(@RequestParam String myId,
                                      @RequestParam Integer page,
                                      @RequestParam Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = vlogService.getMyFriendVlogList(myId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }



}
