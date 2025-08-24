package com.imooc.repository;

import com.imooc.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//mongodb数据访问层使用自定义的mapper，继承其mapper接口MongoRepository
/**
 * 继承的MongoRepository接口中，
 * 泛型1是对应的domain包中的实体类
 * 泛型2是该类的对应的文档主键
 * 记得贴上Repository注解，该类的对象交由spring容器管理
 */
@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {

    /***
     * Pageable：代表具有可分页功能的类，和Page<>类差不多，需要自己传入page和pagesize去创建该对象
     * 注意包别导错了，在spring.domain下，是spring提供的类
     * 通过实现Repository，自定义条件查询，方法名就代表了具体的条件，底层会自动去实现自定义方法
      */
/*
    根据用户Id进行查询该用户的所有消息，并由创建时间进行排序返回

 */
    List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(String toUserId,
                                                                 Pageable pageable);
//    void deleteAllByFromUserIdAndToUserIdAndMsgType();
}
