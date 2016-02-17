package com.congxiaoyao;

import com.congxiaoyao.cmd.*;
import com.congxiaoyao.handler.CommandWindowHandler;
import com.congxiaoyao.handler.HelpHandler;

public class MainClass {

	private static Analysable analyzer;
	private static CommandWindow window;

    public static void main(String[] args) {
        //创建一个CommandWindow实例并显示出来
        window = new CommandWindow().setVisible();
        //监听每一次用户提交的输入并交由Analysable对象处理
        window.setOnSubmitListener((content -> analyzer.process(content)));

        //绑定命令的处理函数所在的类的实例，可以是多个
        analyzer = CommandAnalyzerManager.getInstance();
        CommandAnalyzerManager.handleWith(new MainClass());
        CommandAnalyzerManager.handleWith(new HelpHandler());
        CommandAnalyzerManager.handleWith(new CommandWindowHandler(window));

        //动态的添加和删除命令
        analyzer.addCommand(new Command("welcome"));
        analyzer.process("welcome");
        analyzer.removeCommand(new Command("welcome"));

//		一句话即可支持代码提示
//		window.setAssistant(new CodeAssistant(analyzer.getCommands()));
    }

	@CommandName
    public final void handleWelcome(Command command) {
		System.out.println("\n\n本demo展示了这套框架的基本使用方法\n"
				+ "这里添加了一些有关这个窗口的操作的命令\n"
				+ "并且已经实现了相应功能，可以输入help进行查看\n"
				+ "具体工作原理及使用方式见CommandAnalyzer类头注释\n"
				+ "\4");
	}

    @CommandName
    public void handleTest() {
        System.out.println("handleTest()");
    }
}