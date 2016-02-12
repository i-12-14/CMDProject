package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.CommandAnalyzerManager;

/**
 * 统一handler的标准为所有子handler提供getAnalysable方法并要求他们实现命令的动态添加方法
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public abstract class CommandHandler {

    abstract CommandHandler registerCommands();

    protected Analysable getAnalysable() {
        return CommandAnalyzerManager.getInstance();
    }
}
