package com.cmd.core;

/**
 * 用于抽象各个基本数据类型的包装类型的parse函数的接口
 *
 * @version 2.1
 * Created by congxiaoyao on 2016/2/22.
 */
public interface StringParser {

    /**
     * @param arg
     * @return 将arg转换为一种基本数据类型的包装类
     */
    Object prase(String arg);
}