package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感参数拦截
 * 对于某些处理函数，我们有时只关心这个参数的一些特定值
 * 如设置窗口状态命令，我们只关心全屏或隐藏标题最大化这三个参数
 * 对于其他的参数，我们希望屏蔽掉。此注解就是用于过滤单个特定值
 * 同时需要配合{@code @CommandName}或{@code @CmdDef}一同使用
 * 下面举出几种使用方法
 * <pre>
 *
 * 对于含有两个参数的命令foo，假设我们只希望处理第二个参数为sensitive时的情况，那么他的处理函数可以定义如下
 * <code>@CommandName("foo")</code>
 * public void foo(MyString arg1, @OnlyCare("sensitive")MyString arg2){
 *     System.out.println("foo XXX sensitive");
 * }
 * 只要将所关心的内容通过OnlyCare注解标注在方法的参数前即可实现敏感参数拦截
 *
 * 对于一参命令foo，允许一种特殊的定义方式以简化书写(少写处理函数的参数声明)
 * 也就是说这个处理函数不是用来处理无参命令的，即使这个函数是无参的
 * <code>@CommandName("foo")</code>
 * <code>@OnlyCare("sensitive")</code>
 * public void foo(){
 *     System.out.println("foo sensitive");
 * }
 *
 * 对于一个及以上参数的命令foo，可以把函数的参数列表里的参数名当做OnlyCare的参数从而省略OnlyCare的参数
 * <code>@OnlyCare("sensitive")</code>
 * public void foo( @OnlyCare String hello, @OnlyCare String world){
 *     System.out.println("foo hello world");
 * }
 * 注意要想使用这个特性请在编译时使用javac的-parameters参数
 *
 * </pre>
 *
 * @author congxiaoyao
 * @version 2.0
 * @date 2016.1.24
 * @see CommandName
 */

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnlyCare {

	String value() default "";

}
