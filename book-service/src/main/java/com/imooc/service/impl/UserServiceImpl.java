package com.imooc.service.impl;


import com.imooc.bo.UpdatedUserBO;
import com.imooc.enums.UserInfoModifyType;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.DesensitizationUtil;
import org.apache.ibatis.annotations.Mapper;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import com.imooc.enums.Sex;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private Sid sid;
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";


    @Override
    /**

    * Description:返回用户是否存在

    * date: 2022/5/7 11:06

    * @author: sfh

    * @since JDK 1.8

    */
    public Users queryMobileIsExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        //将选择条件赋给Example对象，类似于Querywarpper
        criteria.andEqualTo("mobile",mobile);
        //将Example传入进行查询
        Users users = usersMapper.selectOneByExample(userExample);
        return users;
    }

    /***
     * 用户不存在创建用户
     * @param mobile
     * @return
     */

    @Override
    public Users createUser(String mobile) {
        //要获得全局的唯一的id，用户分库分表也不能重复，所以不能使用主键自增策略
        String userId = sid.nextShort();
        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        //业务层创建用户之后通过ORM直接插入到数据库中
        usersMapper.insert(user);

        return user;
    }

    @Override
    public Users getUser(String userId) {
        //调用统一mapper接口的根据主键查询方法，相当于MP的selectById方法
        Users user = usersMapper.selectByPrimaryKey(userId);
        return user;
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO) {
        //业务层处理BO对象，赋值（降级）为Mapper传输对象即Users，交给Mapper进行pojo的Dao操作
        Users pendingUser = new Users();
        //将业务层对象的属性赋值给Mapper层对象，因为mapper不能接受BO对象，没有的就复制过去null，也不会更新，userid是必须的
        BeanUtils.copyProperties(updatedUserBO, pendingUser);
        //接收的参数为对应于数据库的实体类对象，利用字段的自动匹配进行更新表的操作，如果传入obj对象中的某个属性值为null，则不进行数据库对应字段的更新。
        int res = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if(res != 1){
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);
        }

        //返回更新后的user对象
        return getUser(updatedUserBO.getId());
    }

    @Override
    public Users updateUserInfo(UpdatedUserBO updatedUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        if(type == UserInfoModifyType.NICKNAME.type){
            //加入条件查询
            criteria.andEqualTo("nickname",updatedUserBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            //判断nickname的用户是否存在
            if(user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        if(type == UserInfoModifyType.IMOOCNUM.type){
            criteria.andEqualTo("imoocNum",updatedUserBO.getImoocNum());
            Users user = usersMapper.selectOneByExample(example);
            if(user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            //下面判断imooc号是否能够通过只能修改一次的条件判断（先满足慕课号不存在）
            Users tempUser = getUser(updatedUserBO.getId());
            //都不再直接使用值，而是都对常量进行了封装
            if(tempUser.getCanImoocNumBeUpdated() == YesOrNo.NO.type){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
        }
        //双参数业务方法校验完之后进行单参数方法的正式修改
        return updateUserInfo(updatedUserBO);
    }
}
