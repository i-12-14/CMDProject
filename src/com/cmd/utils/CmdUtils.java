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
 * @version 2.1
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
        String half = completely.substring(0, completely.indexOf('('));
        String[] methodInfos = half.split(" ");
        String methodName = methodInfos[methodInfos.length - 1];
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
        String org = getSimpleMethodSignature(method);
        String simple = org.substring(0, org.indexOf('('));
        String[] split = simple.split(" ");
        return org.replace(simple, split[split.length - 1]);
    }

    /*******************************************************************
     * 这一大段是一个算法，实现通过一个字符来分割字符串的功能
     * java提供的split函数在这种情况下效率低到令人发指，于是手写了递归版分割算法
     * 返回被分割出来的数组
     */
    private static String[] result = null;  //用于保存分割的结果
    private static char[] content = null;   //待分割的字符串的char数组
    private static char delimiter = 0;         //分隔符
    private static int end = 0;             //对content字串分析的结束位置

    /**
     * 整个函数对静态变量content做分析，当函数运行结束，结果保存在result数组中
     * @param start 从content的哪一个位置开始分析
     * @param putIndex 将查找出来的内容放到result数组中的哪一个位置
     */
    private static void split(int start,int putIndex) {
        //处理分隔符在末尾的情况
        if(start >= end){
            result = new String[putIndex];
            return;
        }
        //寻找分隔符，如果找到的话先让分割符后面的内容继续去寻找新的分隔符位置
        //再把自分隔符前面的内容放入result数组
        for (int i = start; i < end; i++) {
            if (content[i] == delimiter) {
                split(i + 1,putIndex + 1);
                result[putIndex] = new String(content, start, i - start);
                return;
            }
        }
        //到达了字串的末尾仍然没有找到分隔符，那可以向上层返回了
        result = new String[putIndex + 1];
        result[putIndex] = new String(content, start, end - start);
    }

    /**
     * 真正可以提供给用户用的字串分割函数，通过一个char来分割字串
     * 实现依赖private的重载子函数，递归分割
     * @param content 要被分割的字串的char数组
     * @param start 起始位置
     * @param delimiter 分隔符
     * @return 分割后的内容的数组 与jdk的split函数返回一样
     */
    public static String[] split(char[] content, int start, char delimiter) {
        if (content.length == 1 && content[0] == delimiter) {
            return new String[0];
        }
        CmdUtils.content = content;
        CmdUtils.delimiter = delimiter;
        CmdUtils.end = content.length;
        split(start, 0);
        return result;
    }

    /**
     * 与上个函数不同的是这个函数默认指定开始分割的起始位置为0
     * @param content 要被分割的字串的char数组
     * @param delimiter 分隔符
     * @return 分割后的内容的数组 与jdk的split函数返回一样
     */
    public static String[] split(char[] content, char delimiter) {
        split(content, 0, delimiter);
        return result;
    }
    //******************************************************************
}