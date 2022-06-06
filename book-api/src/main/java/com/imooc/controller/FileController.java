package com.imooc.controller;


import com.imooc.MinIOConfig;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.utils.MinIOUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@Api(tags = "文件上传接口FileController")
public class FileController {
    @Autowired
    private MinIOConfig minIOConfig;
//    @PostMapping("upload")
//    public GraceJSONResult upload(MultipartFile file) throws Exception {
//        String fileName = file.getOriginalFilename();
//        MinIOUtils.uploadFile(minIOConfig.getBucketName(), fileName, file.getInputStream());
//        //获取生成的图片链接，进行返回
//        String imgUrl = minIOConfig.getFileHost() + "/"+ minIOConfig.getBucketName()+ "/" +fileName;
//        return GraceJSONResult.ok(imgUrl);
//    }
}
