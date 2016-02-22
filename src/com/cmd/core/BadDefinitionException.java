package com.cmd.core;

/**
 * 当用户的注解使用不当或处理函数的定义存在问题时会抛出磁异常
 * 产生异常的原因有
 * <ul>
 * <li>在处理函数中带参数的情况下将OnlyCare标记在函数上
 * <li>将OnlyCare标记在了可变参数(Command类型或String[]类型)上
 * <li>将CmdDef注解与另外的三个注解混合使用
 * <li>处理函数中同时存在可变参数及不可变参数
 * </ul>
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/22.
 */
public class BadDefinitionException extends Exception {

	private static final long serialVersionUID = 5446979037535382391L;

	private static final String ERROR = "your definition is illegal\n";

    public static final String ONLYCARE_ERROR = "OnlyCare inject error at\n";

    public static final String DECLARE_ERROR = "Command inject error at\n";

    public BadDefinitionException() {
        super(ERROR);
    }

    public BadDefinitionException(String message, String methodName) {
        super(message + methodName);
    }
}
