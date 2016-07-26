package com.cmd;

import com.cmd.annotations.CommandName;
import com.cmd.core.Command;
import com.cmd.core.FastAnalyzer;
import com.cmd.extras.CommandWindow;
import com.cmd.handler.CommandWindowHandler;
import com.cmd.handler.DynamicCommandHandler;
import com.cmd.handler.HelpHandler;

public class Demo {

	private static FastAnalyzer analyzer;
	private static CommandWindow window;
    public static void main(String[] args) {
        //创建一个CommandWindow实例并显示出来
        window = new CommandWindow().setVisible();
        //监听每一次用户提交的输入并交由Analysable对象处理
        window.setOnSubmitListener((content -> System.out.println(analyzer.process(content))));

        //绑定命令的处理函数所在的类的实例，可以是多个
        analyzer = FastAnalyzer.handleWith(new Demo());
        //使CommandAnalyzer支持热加载
        analyzer.addHandlingObject(new DynamicCommandHandler(analyzer));
        analyzer.addHandlingObject(new HelpHandler(analyzer));
        analyzer.addHandlingObject(new CommandWindowHandler(window,analyzer));

        analyzer.process("welcome");
        analyzer.removeCommand(new Command("welcome"));

//		一句话即可支持代码提示
//		window.setAssistant(new CodeAssistant(analyzer.getCommands()));
    }

	@CommandName
    public final void welcome(Command command) {
        System.out.println("\n\n本demo展示了这套框架的基本使用方法\n"
                + "这里添加了一些有关这个窗口的操作的命令\n"
                + "并且已经实现了相应功能，可以输入help进行查看\n"
                + "详情见com.cmd.core.CommandAnalyzer类头注释\n" + "\4");
    }
}