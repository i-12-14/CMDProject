package com.cmd.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令以Command对象的形式进行管理，可由commandName、delimiter两个字段唯一确定一个Command对象
 * 一般来说Command对象由CommandAnalyzer根据注解及处理函数等信息构造
 * Command中保存了处理这条命令的处理函数的集合，以便CommandAnalyzer分析调用
 *
 * @author congxiaoyao
 * @version 2.4
 * @date 2016.1.19
 */

public class Command {

    public String commandName;
    public String delimiter;
    public String[] parameters;

    private List<HandlingMethod> handlingMethods;

    /**
     * 这里强制规定了一条命令的格式 形如：[commandName][delimiter][parameter][delimiter][parameter]...
     * @param commandName 命令头
     * @param delimiter   分隔符
     */
    public Command(String commandName, String delimiter) {
        this.commandName = commandName;
        this.delimiter = delimiter;
        handlingMethods = new ArrayList<>(1);
    }

    public Command(String commandName) {
        this(commandName, " ");
    }

    /**
     * 判断这条命令是否是一条无参命令
     * 无参命令的判定是根据其所持有的所有的HandlingMethod的参数个数判定的
     * @return 如果所有的HandlingMethod都是无参函数，返回true，否则false
     * @throws NoneHandlingMethodException
     */
    public boolean isNullParamCommand() throws NoneHandlingMethodException {
        if (handlingMethods.size() == 0) {
            throw new NoneHandlingMethodException(toString());
        }
        for (HandlingMethod handlingMethod : handlingMethods) {
            if (handlingMethod.getParamCount() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param commandName
     * @return 跟一参的构造函数构造出来的一样
     */
    public static Command forName(String commandName) {
        return new Command(commandName);
    }

    /**
     * 设置delimiter
     *
     * @param delimiter
     * @return
     */
    public Command delimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * @return 所有备选的HandlingMethod
     */
    public List<HandlingMethod> getHandlingMethods() {
        return handlingMethods;
    }

    /**
     * 添加一个处理函数
     * @param method
     */
    public void addHandlingMethod(HandlingMethod method) throws IllegalHandlingMethodException {
        if (!method.isLegal()) {
            throw new IllegalHandlingMethodException(method.method.toString());
        }
        handlingMethods.add(method);
    }

    /**
     * 给类内维护的HandlingMethods按照OnlyCare的数量由多到少排序
     */
    public void sortHandlingMethod() {
        Collections.sort(handlingMethods, (method1, method2)->
                method2.getOnlyCareCount() - method1.getOnlyCareCount());
    }

    /**
     * @param obj
     * @return 如果两者的commandName与delimiter对应相等，我们认为相等 返回true
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Command) {
            Command command = (Command) obj;
            if (command.commandName.equals(commandName)
                    && isDelimiterEquals(command.delimiter)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDelimiterEquals(String otherDelimiter) {
        if (otherDelimiter == null && delimiter == null) return true;
        if (otherDelimiter != null && delimiter != null)
            return delimiter.equals(otherDelimiter);
        return false;
    }

    @Override
    public String toString() {
        return "Command{" +
                "commandName='" + commandName + '\'' +
                ", delimiter='" + delimiter + '\'' +
                '}';
    }
}
