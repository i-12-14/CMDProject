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
     * 这条命令的名字，如果使用默认值，是默认值将使用函数名作为命令名
     * @see CommandName
     * @return
     */
    String commandName() default "";

    /**
     * 定义这条命令的分隔符 默认为空格
     * 如果分隔符属于转义字符，将自动转义
     * 若定义为null 对于一参或无参命令，CommandAnalyzer在处理的时候将忽略分隔符
     * @return
     */
    String delimiter() default " ";

    /**
     * 对于这个handlingMethod的描述，如果不写将以函数名作为description
     * @return
     */
    String description() default Description.DEFAULT_VALUE;

}