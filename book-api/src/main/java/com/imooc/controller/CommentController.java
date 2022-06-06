package com.imooc.controller;


import com.imooc.bo.CommentBO;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.service.CommentService;

import com.imooc.service.VlogService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Api(tags = "CommentController 评论模块的接口")
@RequestMapping("comment")
@RestController
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;

    @Autowired
    private VlogService vlogService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception {
        CommentVO commentVO = commentService.createComment(commentBO);
        //业务层处理完之后，返回给前端该评论对象，即默认把发表的评论放在第一个，当用户重新打开评论区时，才会重新刷新列表
        return GraceJSONResult.ok(commentVO);
    }
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) {

        String countsStr = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }

        return GraceJSONResult.ok(Integer.valueOf(countsStr));
    }

    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String vlogId,
                                @RequestParam(defaultValue = "") String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {

        return GraceJSONResult.ok(
                commentService.queryVlogComments(
                        vlogId,
                        userId,
                        page,
                        pageSize));
    }
    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                  @RequestParam String commentId,
                                  @RequestParam String vlogId) {
        commentService.deleteComment(commentUserId,
                commentId,
                vlogId);
        return GraceJSONResult.ok();
    }


}
