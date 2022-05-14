package com.imooc.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.bo.VlogBO;
import com.imooc.enums.MessageEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mapper.MyLikedVlogMapper;
import com.imooc.mapper.VlogMapper;
import com.imooc.mapper.VlogMapperCustom;
import com.imooc.pojo.MyLikedVlog;
import com.imooc.pojo.Vlog;
import com.imooc.service.FansService;
import com.imooc.service.MsgService;
import com.imooc.service.VlogService;
import com.imooc.base.BaseInfoProperties;
import com.imooc.utils.PagedGridResult;
import com.imooc.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {
    @Autowired
    private VlogMapper vlogMapper;
    @Autowired
    private Sid sid;
    @Autowired
    private VlogMapperCustom vlogMapperCustom;
    @Autowired
    private FansService fansService;
    @Autowired
    private MsgService msgService;
    /**

    * Description:创建Vlog

    * date: 2022/5/9 17:39

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public void createVlog(VlogBO vlogBO) {
        //返回固定16位的字母数字混编的字符串,视频id。
        String vid = sid.nextShort();
        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);
        vlog.setId(vid);
        //新视频点赞和评论均为0
        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);
        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());
        vlogMapper.insert(vlog);
    }
    /**

    * Description:获取主页或搜索视频,只是展示主页瀑布流，具体搜索的视频的展示需要调用id查询
     * 注意后端方法接收的形参数量可以不等于前端的queryString数量，使用注解进行接收
     * 大于前端数量那么没赋值的就为null，如果小于的话，方法就不会被接受，调用方法是由Controller的@RequestMapping映射决定的

    * date: 2022/5/9 17:39

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public PagedGridResult getIndexVlogList(String userId,String search, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        HashMap<String, String> map = new HashMap<>();
        if(!StringUtils.isBlank(search)){
            map.put("search", search);
        }
        List<IndexVlogVO> indexVlogList = vlogMapperCustom.getIndexVlogList(map);
        /***
         * 由于在获取主页/搜索的视频的内容时，仅对视频进行了展示，并没有对是否点赞进行查询并和前端进行交互
         * 且因为前端也没有专门在访问一个i新视频时发查询请求，所以不需要去创建新业务，直接在具体展示的视频的接口实现上
         * 增加对点赞等信息的查询和返回,不管何种查询方式视频一定是从列表进行返回的，所以对list中所有视频元素进行点赞信息判断
         */
        for (IndexVlogVO v : indexVlogList) {

            /***
             * 通过查询用户的附加信息，对原来接口进行扩展，返回一些需要的新信息，完善了获取新视频时前端关注和点赞爱心展示的功能
             * 在获取到视频列表的时候就提前判断好了点赞等附加信息，如果用户下拉刷新或者点击主页刷新，那么都会重新对视频列表进行加载附加信息
             * 注意：有专门的接口去处理每一条视频的点赞数量信息，因为获取列表但没刷到的时候如果其他人点赞了视频，刷到的时候的数量还是获取视频列表时加载的数量
             * 相当于获取时缓存好的数量（也可以当作双保险吧）
             */
            setterVO(v, userId);
        }
        return setterPagedGrid(indexVlogList, page);
    }
    /**
     * 根据视频主键查询vlog
     */
    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {

        Map<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);

        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        //三项判断
        if (list != null && list.size() > 0 && !list.isEmpty()) {
            IndexVlogVO vlogVO = list.get(0);
            setterVO(vlogVO, userId);
            return vlogVO;
        }

        GraceException.display(ResponseStatusEnum.VIDEO_NOT_EXIST);
        return null;
    }
    //设置用户的作品的私密性，不需要返回值
    @Override
    public void changeToPrivateOrPublic(String userId, String vlogId, Integer yesOrNo) {
        //创建查找条件即Example对象
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",vlogId);
        criteria.andEqualTo("vlogerId",vlogId);
        //创建更新的属性（用对象进行承载）
        Vlog vlog = new Vlog();
        vlog.setIsPrivate(yesOrNo);
        //根据条件找到的数据，通过对象进行更新，selective使属性为null，不更新
        vlogMapper.updateByExampleSelective(vlog, example);

    }
    /**

    * Description:作品列表都需要进行分页展示
     * Grid:网格的（也就是分页产生的）
     * Custom：特殊的，定制的

    * date: 2022/5/9 20:19

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {
        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isPrivate",yesOrNo);
        //注意：userId和clogerId是外键，只不过不是物理的
        criteria.andEqualTo("vlogerId",userId);
        PageHelper.startPage(page, pageSize);
        List<Vlog> vlogs = vlogMapper.selectByExample(example);
        return setterPagedGrid(vlogs, page);
    }
    /**

    * Description:点赞操作，不需要返回值

    * date: 2022/5/10 20:23

    * @author: sfh

    * @since JDK 1.8

    */
    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;
    @Override
    public void userLikeVlog(String userId, String vlogerId,String vlogId) {
        String rid = sid.nextShort();

        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setId(rid);
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);

        myLikedVlogMapper.insert(likedVlog);


        // 系统消息：点赞短视频
        Vlog vlog = this.getVlog(vlogId);
        Map msgContent = new HashMap();
        //放入id
        msgContent.put("vlogId", vlogId);
        //放入cover封面
        msgContent.put("vlogCover", vlog.getCover());
        msgService.createMsg(userId,
                vlog.getVlogerId(),
                MessageEnum.LIKE_VLOG.type,
                msgContent);

        // 点赞后，视频和视频发布者的获赞都会 +1，对于数量的统计不可能用DB.count，所以计数一定保存到redis中
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        /***
         * 我点赞的视频，需要在redis中保存关联关系，因为在刷到一条视频的时候，要判断我的爱心是否被点亮
         * 我的关系是要放到数据库里的，但是在查询时不可能一直查DB，只需要在对DB做一次操作的时候对redis进行操作即可
         * 但是比如如果用户频繁的点赞和取消点赞，就会增加会点赞关系DB的访问，所以同样要使用后面的其他消峰策略
         */
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");


    }

    @Override
    public void userUnLikeVlog(String userId, String vlogerId,String vlogId) {
        MyLikedVlog likedVlog = new MyLikedVlog();
        likedVlog.setVlogId(vlogId);
        likedVlog.setUserId(userId);
        //根据vlogId和userId将数据库数据删除， 该数据只能存在一条
        // 当然用主键或者完整的对象（会映射到数据库）也可以查到该唯一的数据源，这里前端因为要校验传的两个字段正好够用
        myLikedVlogMapper.delete(likedVlog);
        //操作redis数据或关系
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);


    }
    /**

    * Description:工具方法，查询用户是否点赞了当前视频，即去redis查关系

    * date: 2022/5/10 20:50

    * @author: sfh

    * @since JDK 1.8

    */
    private boolean doILikeVlog(String myId, String vlogId) {

        String doILike = redis.get(REDIS_USER_LIKE_VLOG + ":" + myId + ":" + vlogId);
        boolean isLike = false;
        //注意使用的方法是equalsIgnoreCase，不是==，因为在redis使用的使String进行存储的，value一定是字符串而不是int
        if (StringUtils.isNotBlank(doILike) && doILike.equalsIgnoreCase("1")) {
            isLike = true;
        }
        return isLike;
    }
    /**

    * Description:获取某视频被点赞的数量，在获取视频列表时，就对是视频对象添加这个属性信息

    * date: 2022/5/10 21:13

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public Integer getVlogBeLikedCounts(String vlogId) {
        String countsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        if (StringUtils.isBlank(countsStr)) {
            countsStr = "0";
        }
        //返回一个int（Integer）类型的数值对象
        return Integer.valueOf(countsStr);
    }

    @Override
    public PagedGridResult getMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFollowVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);

        for (IndexVlogVO v : list) {
            String vlogerId = v.getVlogerId();
            String vlogId = v.getVlogId();

            if (StringUtils.isNotBlank(myId)) {
                // 用户必定关注该博主
                v.setDoIFollowVloger(true);

                // 判断当前用户是否点赞过视频
                v.setDoILikeThisVlog(doILikeVlog(myId, vlogId));
            }

            // 获得当前视频被点赞过的总数
            v.setLikeCounts(getVlogBeLikedCounts(vlogId));
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult getMyFriendVlogList(String myId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);

        for (IndexVlogVO v : list) {
            setterVO(v, myId);
        }

        return setterPagedGrid(list, page);
    }

    @Override
    public Vlog getVlog(String id) {
        return vlogMapper.selectByPrimaryKey(id);
    }
    /**

    * Description:到达nacos阈值对点赞数进行入库统计，评论等数量都可以采用该方法

    * date: 2022/5/13 15:09

    * @author: sfh

    * @since JDK 1.8

    */
    @Override
    public void flushCounts(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);

        vlogMapper.updateByPrimaryKeySelective(vlog);
    }

    /**

    * Description:在加载视频详情detailById时，将信息进行加载
     * 设置index视频的附加信息加载，参数是IndexVlogVO

    * date: 2022/5/11 10:28

    * @author: sfh

    * @since JDK 1.8

    */
    private IndexVlogVO setterVO(IndexVlogVO v, String userId) {
        String vlogerId = v.getVlogerId();
        String vlogId = v.getVlogId();
        /***
         * 通过查询用户的附加信息，对原来接口进行扩展，返回一些需要的新信息，完善了获取新视频时前端关注和点赞爱心展示的功能
         * 在获取到视频列表的时候就提前判断好了点赞等附加信息，如果用户下拉刷新或者点击主页刷新，那么都会重新对视频列表进行加载附加信息
         * 注意：有专门的接口去处理每一条视频的点赞数量信息，因为获取列表但没刷到的时候如果其他人点赞了视频，刷到的时候的数量还是获取视频列表时加载的数量
         * 相当于获取时缓存好的数量（也可以当作双保险吧）
         */
        if (StringUtils.isNotBlank(userId)) {
            // 用户是否关注该博主
            boolean doIFollowVloger = fansService.queryDoIFollowVloger(userId, vlogerId);
            v.setDoIFollowVloger(doIFollowVloger);

            // 判断当前用户是否点赞过视频
            v.setDoILikeThisVlog(doILikeVlog(userId, vlogId));
        }

        // 获得当前视频被点赞过的总数（Integer类型），getVlogBeLikedCounts业务方法会专门去处理实时的点赞数量
        v.setLikeCounts(getVlogBeLikedCounts(vlogId));

        return v;
    }
}
