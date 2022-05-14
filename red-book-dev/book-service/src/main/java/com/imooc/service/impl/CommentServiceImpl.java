package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;

import com.imooc.bo.CommentBO;
import com.imooc.enums.YesOrNo;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.mapper.CommentMapper;

import com.imooc.mapper.CommentMapperCustom;
import com.imooc.pojo.Comment;
import com.imooc.service.CommentService;

import com.imooc.service.VlogService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private VlogService vlogService;
    @Autowired
    private Sid sid;
    @Autowired
    private CommentMapperCustom commentMapperCustom;


    @Override
    public CommentVO createComment(CommentBO commentBO) {
        String commentId = sid.nextShort();
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentBO, comment);
        comment.setId(commentId);
//        comment.setVlogId(commentBO.getVlogId());
//        comment.setVlogerId(commentBO.getVlogerId());
//        comment.setCommentUserId(commentBO.getCommentUserId());
//        comment.setFatherCommentId(commentBO.getFatherCommentId());
//        comment.setContent(commentBO.getContent());
        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        // redis操作放在service中，评论总数的累加
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId(), 1);

        // 留言后的最新评论需要返回给前端进行展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        return commentVO;
    }

    @Override
    public PagedGridResult queryVlogComments(String vlogId, String userId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        PageHelper.startPage(page, pageSize);

        List<CommentVO> list = commentMapperCustom.getCommentList(map);
        /***
         * 对评论列表中的所有评论的附加信息进行加载
         */
        for (CommentVO cv:list) {
            String commentId = cv.getCommentId();

            // 当前短视频的某个评论的点赞总数
            String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId);
            Integer counts = 0;
            if (StringUtils.isNotBlank(countsStr)) {
                counts = Integer.valueOf(countsStr);
            }
            cv.setLikeCounts(counts);

            // 判断当前用户是否点赞过该评论
            String doILike = redis.hget(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);
            //为了执行忽略大小写的比较，使用equalsIgnoreCase方法
            if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
                cv.setIsLike(YesOrNo.YES.type);
            }
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        Comment pendingDelete = new Comment();
        pendingDelete.setId(commentId);
        pendingDelete.setCommentUserId(commentUserId);

        commentMapper.delete(pendingDelete);

        // 评论总数的累减
        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId, 1);
    }
    /**

    * Description:获得根据id获得评论

    * date: 2022/5/11 10:43

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public Comment getComment(String id) {
        return commentMapper.selectByPrimaryKey(id);
    }
    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String commentId,
                                @RequestParam String userId) {
        /***
         * 为什么使用hash进行存储，REDIS_VLOG_COMMENT_LIKED_COUNTS即视频评论被喜欢的数量
         * 具体定位到哈希桶的位置即commentId，即具体评论，将其点赞数加一，我们不可能去用String，一个评论一个key-value
         */
        // 故意犯错，bigkey，为了避免该问题，可以用Restful传入视频id进行分类，或者用前面的“：”+commentId，一个评论一个数据
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId, "1");
//        redis.hset(REDIS_USER_LIKE_COMMENT, userId, "1");


//        // 系统消息：点赞评论
//        Comment comment = commentService.getComment(commentId);
//        Vlog vlog = vlogService.getVlog(comment.getVlogId());
//        Map msgContent = new HashMap();
//        msgContent.put("vlogId", vlog.getId());
//        msgContent.put("vlogCover", vlog.getCover());
//        msgContent.put("commentId", commentId);
//        msgService.createMsg(userId,
//                comment.getCommentUserId(),
//                MessageEnum.LIKE_COMMENT.type,
//                msgContent);


        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String commentId,
                                  @RequestParam String userId) {

        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" + commentId);

        return GraceJSONResult.ok();
    }
}
