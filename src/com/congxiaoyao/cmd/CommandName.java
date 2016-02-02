package com.congxiaoyao.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注释用于绑定处理函数与被处理的命令，使得CommandAnalyzer可以通过此注释找到命令与处理函数的联系
 * 参数中只需要写入命令名即可，如果不写具体值，CommandAnalyzer会尝试从函数名称中解析这条命令
 * 所以，如果按照命名规范来写函数名，是可以不加参数的
 * 例如命令 help 处理函数 handleHelp
 * 要求命令只由小写字母或数字组成，字母开头，处理函数在命令名前面加handle并将命令名的首字母大写
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
	public String value() default "";
	
}