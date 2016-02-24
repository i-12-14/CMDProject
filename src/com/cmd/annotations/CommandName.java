package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于绑定处理函数与被处理的命令，使得CommandAnalyzer可以通过此注释找到命令与处理函数的联系
 * 参数中只需要写入命令名即可，如果不写具体值，CommandAnalyzer将使用函数名作为命令名
 * 注意请不要与{@code CmdDef}一起使用，但可以配合另外两个注解一同使用
 *
 * @see Description
 * @see Delimiter
 *
 * @see CmdDef
 *
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.1.20
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface CommandName {
	
	/**
	 * 只要将命令名作为注解的参数写入即可完成处理函数与命令的绑定
	 * @return
	 */
	String value() default "";
	
}