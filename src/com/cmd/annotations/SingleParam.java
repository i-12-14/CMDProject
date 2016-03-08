package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解为了解决一种尴尬的情况
 * 如果定义了一条发送信息的命令，命令名为send 分割符为空格 同时定义了一参处理函数
 * 有一种特殊的情况是发送的内容中带有空格，这样框架会将用户输入解析为多参命令从而寻找多参处理函数并调用
 * 如果我们不把处理函数定义为可变参数类型将无法处理用户的这条输入
 * 当然，我们可以使用一个用户永远也不可能输入的分隔符或直接将分隔符定义为null
 * 但这总不是一种优雅的解决方案
 * 于是，对于标记此注解的一参处理函数，框架将赋予它处理多参命令的能力
 * 即第一个分隔符之后的所有内容都被解析为一个参数，不管里面包不包含分隔符
 *
 * @version 2.4.2
 * Created by congxiaoyao on 2016/3/8.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface SingleParam {

}