package com.congxiaoyao.cmd;

import java.util.ArrayList;

/**
 * 给整个包用的工具类
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class CmdUtils {

    private static ArrayList<String> params = new ArrayList<>(5);

    /**
     * @param command
     * @return 通过一个命令对象生成一条可以在文件中定义他的字符串
     */
    public static String parseCommandIntoString(Command command) {
        return command.toDefinitionString();
    }

    /**
     * @param cmd
     * @return 将定义在文件中的一个命令解析为Command对象
     */
    public static Command getCommand(String cmd) {
        //将单引号引起来的内容一个一个的加入params中,第一步就是清掉上一次可能遗留在params中的内容
        params.clear();
        int start = cmd.indexOf('\'');
        while(start != -1) {
            String param = cmd.substring(start + 1 , start = cmd.indexOf('\'',start + 1));
            start = cmd.indexOf('\'',start + 1);
            params.add(param);
        }
        //循环读取结束，开始根据参数个数创建对应的Command对象
        int size = params.size();
        Command command = null;
        switch (size) {
            case 1: command = new Command(params.get(0)); break;
            case 2: command = new Command(params.get(0),params.get(1)); break;
            case 3: command = new Command(params.get(0), Integer.parseInt(params.get(1)),
                    params.get(2)); break;
            case 4: String delimiter = characterEscape(params.get(2));
                command = new Command(params.get(0),
                        Integer.parseInt(params.get(1)),
                        delimiter,params.get(3));
                break;
            default: break;
        }
        return command;
    }

    //需要转义的字符
    private static String[] ec = {".","$","^","(",")","[","|","{","?","+","*",};
    /**
     * 分析这个String是否为ec数组中所包含的待转义的字符，如果是就给他转义喽
     * @param ch 待检查String
     * @return 转义后的ch
     */
    static String characterEscape(String ch) {
        for (String string : ec) {
            if(string.equals(ch)) {
                ch = "\\"+string;
                break;
            }
        }
        return ch;
    }

    /**
     * 判断命令是否以给定的单词开始
     * @param beginWord 给定的开始单词
     * @param command 命令语句
     * @param delimiter 分隔符，如果不为null则判断command中从0到分隔符之前的内容是不是beginWord，否则直接判断
     * @return 以给定的单词开始返回true
     */
    static boolean isBeginWith(String beginWord, String command, String delimiter) {
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
     * 得到简化版的方法签名如传入static String com.congxiaoyao.cmd.CmdUtils.getSimpleMethodSignature(String)
     * 将会返回static String getSimpleMethodSignature(String)
     * @param completely
     * @return
     */
    static String getSimpleMethodSignature(String completely) {
        String[] methodInfo= completely.split(" ");
        String methodName = methodInfo[methodInfo.length - 1];
        int end = methodName.indexOf('(');
        methodName = methodName.substring(0, end);
        String[] split = methodName.split("\\.");
        String simple = split[split.length - 1];
        simple = completely.replace(methodName,simple);
        return simple;
    }

    /**
     * 将command中的四个字段的内容追加到StringBuilder中
     * @param command
     * @param builder
     */
    static void appendCommandAttribute(Command command, StringBuilder builder) {
        builder.append("commandName-->").append(command.commandName).append('\n');
        builder.append("paramCount-->").append(command.paramCount).append('\n');
        builder.append("delimiter-->").append(command.delimiter).append('\n');
        builder.append("description-->").append(command.description).append('\n');
    }
}