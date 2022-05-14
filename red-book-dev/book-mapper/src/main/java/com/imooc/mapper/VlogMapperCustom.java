package com.imooc.mapper;

import com.imooc.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom {
    //将函数实参通过@Param注入，在XML中用占位符取值
    //这里的map的value都使用Object，原因是传入的值不一定是String
    List<IndexVlogVO> getIndexVlogList(@Param("paramMap") Map<String,String> map);
    List<IndexVlogVO> getVlogDetailById(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap") Map<String, Object> map);
    List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap") Map<String, Object> map);

}
