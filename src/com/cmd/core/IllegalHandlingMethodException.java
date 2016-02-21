package com.cmd.core;

/**
 * 有一些函数虽然有{@code CommandName}或{@code CmdDef}注解但其参数的定义并不合法，并不能当做处理函数
 * 如果这些函数被解析为处理函数将会抛出此异常
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/20.
 */
public class IllegalHandlingMethodException extends Exception {

    private static final String MSG = "handling method param types error ";

    public IllegalHandlingMethodException() {
        super(MSG);
    }

    public IllegalHandlingMethodException(String message) {
        super(MSG + message);
    }
}
