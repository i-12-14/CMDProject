package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注处理函数的描述信息
 * 单独使用此注解无效，请配合注解{@code CommandName}一起使用
 * @see CommandName
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/19.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Description {

    /**
     * 默认的description，CommandAnalyzer会将这个值替换为函数名，请注意不要踩雷
     */
    String DEFAULT_VALUE = "@METHOD_NAME";

    String value() default DEFAULT_VALUE;
}
