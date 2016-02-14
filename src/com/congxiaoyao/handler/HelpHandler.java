package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.Command;
import com.congxiaoyao.cmd.CommandName;

/**
 * 基于Analysable的getCommandsDescription、getCommandInfo方法提供帮助命令
 * 'help'			'帮助'
 * 'help'	        '1'		'查看每个命令的详细信息'
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class HelpHandler extends BaseHandler {

    private Analysable analysable;

    public HelpHandler(Analysable analysable) {
        this.analysable = analysable;
    }

    public HelpHandler() {
    }

    @Override
    public Analysable getAnalysable() {
        if (analysable != null) {
            return analysable;
        }
        return super.getAnalysable();
    }

    /**
     * 输出帮助信息
     */
    @CommandName
    public void handleHelp() {
        System.out.println("\5" + getAnalysable().getCommandsDescription());
    }

    /**
     * 输出某个特定指令的详细帮助信息 传入-all为输出全部命令的详细帮助信息
     * 强调下框架特性 这个处理函数跟上面的处理函分别处理了重载命令（一参help跟无参help）
     * 巧的是这两个处理函数也是重载的 当然了处理函数不一定也同时重载
     * 只要CommandName注解标注好，即可根据参数个数定位对应处理函数
     * @param commandName
     */
    @CommandName
    public void handleHelp(String commandName) {
        System.out.println("\5" + getAnalysable().getCommandInfo(commandName));
    }

    @Override
    BaseHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("help",	"帮助"));
        analysable.addCommand(new Command("help",	     1,	"查看每个命令的详细信息"));
        return this;
    }
}