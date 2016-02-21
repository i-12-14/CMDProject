package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注一条命令的分割符
 * 单独使用此注解无效，请配合注解{@code CommandName}一起使用
 * 如果分隔符属于转义字符，将自动转义
 * @see CommandName
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/19.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Delimiter {

    /**
     * 定义这条命令的分隔符 默认为空格
     * @return
     */
    String value() default " ";

}
