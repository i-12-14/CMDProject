package com.cmd.handler;

import com.cmd.core.Analysable;
import com.cmd.core.Command;
import com.cmd.core.CommandAnalyzer;

import java.util.List;

/**
 * 统一handler的标准为所有子handler提供getAnalysable方法
 * 好处就是通过这种方式降低类间耦合，使得命令可以一部分一部分的的定义，功能也可以一部分一部分的实现
 *
 * @version 1.0
 * Created by congxiaoyao on 2016/2/13.
 */
public class BaseHandler {

    private static Analysable analysable;

    public BaseHandler(Analysable analysable) {
        this.analysable = analysable;
    }

    protected static Analysable getAnalysable() {
        return analysable;
    }

    public static List<Command> getCommands() {
        return getAnalysable().getCommands();
    }

}
