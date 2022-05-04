package com.vo;

import com.validator.IsMobile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**

* Description:登录入参

* date: 2022/3/26 12:07

* @author: sfh

* @since JDK 1.8

*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loginvo {
    @NotNull
    @IsMobile//自定义注解
    private String mobile;
    @NotNull
    @Length(min = 32)//定义密码长度
    private String password;
}
