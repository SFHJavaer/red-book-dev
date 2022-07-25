package com.imooc;

import com.imooc.utils.MinIOUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class MinIOConfig {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.fileHost}")
    private String fileHost;
    @Value("${minio.bucketName}")
    private String bucketName;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.imgSize}")
    private Integer imgSize;
    @Value("${minio.fileSize}")
    private Integer fileSize;

    @Bean
    /**
     * @description:  创建MinIO客户端使用MinIO的Api，需要提供连接参数
     * @param:
     * @return: com.imooc.utils.MinIOUtils
     * @author Administrator
     * @date: 2022/7/19 20:18
     */
    public MinIOUtils creatMinioClient() {
        return new MinIOUtils(endpoint, bucketName, accessKey, secretKey, imgSize, fileSize);
    }
}
