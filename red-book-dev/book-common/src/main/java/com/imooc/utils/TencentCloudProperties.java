package com.imooc.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
//使用实体类去读取配置文件
@Component
//将类变为pojo
@Data
//指定资源文件具体映射
@PropertySource("classpath:tencentcloud.properties")
//ConfigurationProperties把主配置文件中配置属性设置到对应的Bean属性上。
@ConfigurationProperties(prefix = "tencent.cloud")
public class TencentCloudProperties {

    private String secretId;
    private String secretKey;

}
