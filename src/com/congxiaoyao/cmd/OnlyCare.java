package com.congxiaoyao.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感参数拦截
 * 对于一参处理函数，我们有时只关心这个参数的一些特定值，如设置窗口状态命令，我们只关心全屏或隐藏标题最大化这三个参数
 * 对于其他的参数，我们希望屏蔽掉。此注解就是用于过滤单个特定值，同时需要配合{@code CommandName}一同使用
 * @see CommandName
 * 
 * @version 1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyCare {
	
	public String value() default "";

}