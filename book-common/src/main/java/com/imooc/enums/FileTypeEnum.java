package com.imooc.enums;

/**
 * @Desc: 文件类型 枚举
 * 注意下枚举和注解的区别。注解定义属性的方法和定义方法的格式类似，但是enum和类是类似的
 * enum中所有的实例必须在代码中声明，都是常量的形式，如果是属性的形式，那么也类似于常量，用final定义
 * enum中可以写构造等各种方法，也可以实现多个接口，但是一个注意点：enum继承的基类是Enum而不是Object
 */
public enum FileTypeEnum {
    BGIMG(1, "用户背景图"),
    FACE(2, "用户头像");

    public final Integer type;
    public final String value;

    FileTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }
}
