package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通过此注解绑定一条命令与他的处理函数，将此注解写在处理函数上面即可完成绑定
 * 此注解包含三个参数，皆有默认值，每个参数的意义见方法注释
 * 注意此注解与另外三个注解同时使用时，另外三个注解将失效，他们是
 *
 * @see CommandName
 * @see Delimiter
 * @see Description
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/19.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface CmdDef {

    /**
     * 这条命令的名字，如果使用默认值，将试图解析函数名作为commandName
     * 解析的规则是，如果函数名以handle开头，那handle后面的内容都被认为是命令名(如果出现大写字母会被转换为小写)
     * 如函数定义 public void handleFoo(){}
     * 默认情况下将会解析出命令名 foo
     *
     * @see CommandName
     * @return
     */
    String commandName() default "";

    /**
     * 定义这条命令的分隔符 默认为空格
     * 如果分隔符属于转义字符，将自动转义
     * @return
     */
    String delimiter() default " ";

    /**
     * 对于这个handlingMethod的描述，如果不写将以函数名作为description
     * @return
     */
    String description() default Description.DEFAULT_VALUE;

}
