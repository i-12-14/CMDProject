package com.cmd.handler;

import java.util.HashSet;
import java.util.Set;

import com.cmd.annotations.CommandName;
import com.cmd.annotations.OnlyCare;
import com.cmd.annotations.Outline;
import com.cmd.core.Analysable;

/**
 * 基于Analysable的getCommandsDescription、getCommandInfo方法提供帮助命令
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/13.
 */

@Outline(commandNames = "help", outlines = "帮助信息")
public class HelpHandler extends BaseHandler {

    public HelpHandler(Analysable analysable) {
        super(analysable);
    }

    /**
     * 输出帮助信息
     */
    @CommandName
    public void help() {
        System.out.println("\5" + getAnalysable().getCommandsDescription());
    }

    /**
     * 输出某个特定指令的详细帮助信息
     * 强调下框架特性 这个处理函数跟上面的处理函分别处理了重载命令（一参help跟无参help）
     * 巧的是这两个处理函数也是重载的 当然了处理函数不一定也同时重载
     * 只要CommandName注解标注好，即可根据参数个数定位对应处理函数
     *
     * @param commandName
     */
    @CommandName
    public void help(String commandName) {
        System.out.println("\5" + getAnalysable().getCommandInfo(commandName));
    }

    /**
     * 一参help传入-all为输出全部命令的详细帮助信息
     * 再强调下框架特性，虽然定义的函数是无参函数，但因为OnlyCare的缘故被判定为处理一参命令的处理函数
     * 这里产生的问题是，出现了两个处理相同命令的参数且他们处理的参数个数都是相同的
     * 在这种情况下，CommandAnalyzer会尝试优先调用带有OnlyCare标记的处理函数
     */
    @CommandName("help")
    @OnlyCare("-all")
    public void showAllCommandInfo() {
        StringBuilder builder = new StringBuilder("\5");
        Set<String> commandNames = new HashSet<>();
        getAnalysable().forEachCommand(command -> commandNames.add(command.commandName));
        for (String string : commandNames) {
            builder.append(getAnalysable().getCommandInfo(string));
        }
        System.out.println(builder.toString());
    }
}
