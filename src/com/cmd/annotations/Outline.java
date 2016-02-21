package com.cmd.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对于一条命令的总体描述，这条命令可能存在很多处理函数，可能会分隔符重载，处理函数中或许定义了许多description
 * 但这是对这条命令的总体描述，请注意区分
 * 定义的时候两个数组中的值要一一对应，写起来可能有点反人类，认了吧
 * By the way 这只是可选注解，并不一定要注释，使用时请将其标注在类上
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/21.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)

public @interface Outline {

    /**
     * 一个commandName对应一个outline，也就是对这个commandName的一个概述
     * @return
     */
    String[] commandNames();

    /**
     * outline不同于description，CommandAnalyzer会根据具体情况进行决策选择使用
     * @return
     */
    String[] outlines();

}
