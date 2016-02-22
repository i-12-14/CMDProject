package com.cmd.utils;

import com.cmd.annotations.CommandName;
import com.cmd.annotations.Delimiter;
import com.cmd.annotations.Description;
import com.cmd.core.Command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 给整个工程用的工具类
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class CmdUtils {

    //用于定义一条命令的三个注解
    @SuppressWarnings("unchecked")
	public static Class<? extends Annotation>[] CMD_ANNOTATIONS = new Class[]{
            CommandName.class, Delimiter.class, Description.class};

    //需要转义的字符
    private static String[] ec = {".","$","^","(",")","[","|","{","?","+","*",};

    /**
     * 分析这个String是否为ec数组中所包含的待转义的字符，如果是就给他转义喽
     * @param ch 待检查String
     * @return 转义后的ch
     */
    public static String characterEscape(String ch) {
        for (String string : ec) {
            if(string.equals(ch)) {
                ch = "\\"+string;
                break;
            }
        }
        return ch;
    }

    /**
     * 基本数据类型 这里包括String型了
     */
    public static Class<?>[] baseTypes = {byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class, float.class, Float.class,
            double.class, Double.class, boolean.class, Boolean.class, String.class};

    /**
     * @param type
     * @return 是基本数据类型及其包装类型中的一种返回true
     */
    public static boolean isBaseTypes(Class<?> type) {
        for (Class<?> baseType : baseTypes) {
            if (type == baseType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果参数是Command类型或String数组类型返回true
     * @param type
     * @return 如果是可变参数类型返回true
     */
    public static boolean isVarTypes(Class<?> type) {
        return type == Command.class || type == String[].class;
    }

    /**
     * 判断命令是否以给定的单词开始
     * @param beginWord 给定的开始单词
     * @param command 命令语句
     * @param delimiter 分隔符，如果不为null则判断command中从0到分隔符之前的内容是不是beginWord，否则直接判断
     * @return 以给定的单词开始返回true
     */
    public static boolean isBeginWith(String beginWord, String command, String delimiter) {
        if (beginWord.length() > command.length()) {
            return false;
        }
        if (command.equals(beginWord)) {
            return true;
        }
        return delimiter == null ? command.substring(0, beginWord.length()).equals(beginWord)
                : command.split(delimiter)[0].equals(beginWord);
    }

    /**
     * 得到简化版的方法签名如传入static String com.cmd.utils.CmdUtils.getSimpleMethodSignature(java.lang.String)
     * 将会返回static String getSimpleMethodSignature(String)
     * @param method
     * @return
     */
    public static String getSimpleMethodSignature(Method method) {
        String completely = method.toString();
        String[] methodInfo= completely.split(" ");
        String methodName = methodInfo[methodInfo.length - 1];
        int left = methodName.indexOf('(');
        methodName = methodName.substring(0, left);
        String[] split = methodName.split("\\.");
        String simple = split[split.length - 1];
        simple = completely.replace(methodName,simple);
        return simple.replace(String.class.getName(),"String").replace(Command.class.getName(),"Command");
    }

    /**
     * 比getSimpleMethodSignature还要精简的方法签名只留方法名加参数类型名
     * 如public static String com.cmd.utils.CmdUtils.getSimpleMethodSignature(java.lang.String)
     * 将返回getSimpleMethodSignature(String)
     *
     * @param method
     * @return
     */
    public static String getMoreSimpleMethodSignature(Method method) {
        String simple = getSimpleMethodSignature(method);
        String[] split = simple.split(" ");
        return split[split.length - 1];
    }
}