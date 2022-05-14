package com.imooc.mapper;

import com.imooc.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {
    /**

    * Description:最差情况：使用多表关联查询

    * date: 2022/5/11 11:30

    * @author: sfh

    * @since JDK 1.8

    */
    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);

}