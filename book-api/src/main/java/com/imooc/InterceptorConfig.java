package com.imooc;

import com.imooc.interceptor.PassportInterceptor;
import com.imooc.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//配置完拦截器进行添加到内置
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }

    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //在添加拦截器到WebMvcConfigurer配置中时设置拦截范围，注意只能拦截Controller的请求路径
        //WebMvcConfigurer这个接口其实可以当作是对SpringWeb配置的模块，即实现JavaConfig的配置接口
        registry.addInterceptor(passportInterceptor())
                .addPathPatterns("/passport/getSMSCode");

        registry.addInterceptor(userTokenInterceptor())
                .addPathPatterns("/userInfo/modifyUserInfo")
                .addPathPatterns("/userInfo/modifyImage");
    }
}
