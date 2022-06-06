package com.imooc.repository;

import com.imooc.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
//mongodb数据访问层使用自定义的mapper，继承其mapper接口MongoRepository
@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {

    /***
     * Pageable：代表具有可分页功能的类，和Page<>类差不多，需要自己传入page和pagesize去创建该对象
     * 注意包别导错了，在spring.domain下，是spring提供的类
     * 通过实现Repository，自定义条件查询，方法名就代表了具体的条件，底层会自动去实现自定义方法
      */

    List<MessageMO> findAllByToUserIdEqualsOrderByCreateTimeDesc(String toUserId,
                                                                 Pageable pageable);
//    void deleteAllByFromUserIdAndToUserIdAndMsgType();
}
