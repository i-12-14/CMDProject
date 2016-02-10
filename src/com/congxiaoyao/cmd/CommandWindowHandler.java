package com.congxiaoyao.cmd;

import com.congxiaoyao.cmd.CommandWindow.OnSubmitListener;

import javax.swing.*;

/**
 * 主要为了处理对CommandWindow的操作的命令，如清屏、退出、设置窗口大小提示语等
 * 正常的话这些处理函数应该耦合在CommandWindow所在的类中，这里主要是当做对cmd框架使用的一个简单demo
 * 支持动态添加操作CommandWindow的命令，如果没有在相应文件中声明命令，可调用此函数添加
 * @see #registerCommands(Analysable)
 * 
 * @author congxiaoyao
 * @date 2016.2.2
 * @version 1.1
 */
public class CommandWindowHandler {
	
	private CommandWindow window;

	public CommandWindowHandler(CommandWindow window) {
		this.window = window;
	}

	private static Analysable getAnalyzer() {
		return CommandAnalyzerManager.getInstance();
	}
	
	/**
	 * CommandName注解没有括号也是可以的 但需要以handle开头
	 */
	@CommandName
	public void handleExit() {
		window.closeWindow();
	}
	
	@CommandName("restart")
	public void restartWindow() {
		window.closeWindow();
		window = new CommandWindow().setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalyzer().process(content);
			}
		});
	}
	
	@CommandName
	public void handleVersion() {
		System.out.println("1.0");
	}
	
	@CommandName
	public void handle720P() {
		CommandAnalyzerManager.getInstance().process("bound 1280 720");
		getAnalyzer().process("font 20");
	}
	
	/**
	 * 这就是所谓的参数拦截特性，拦截的是window命令的max参数
	 * @param arg
	 */
	@CommandName("window")
	@OnlyCare("max")
	public void maxSizeWindow(String arg) {
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	@CommandName("window")
	@OnlyCare("full")
	public void handleFull() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(true);
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalyzer().process(content);
			}
		});
	}
	
	@CommandName("window")
	@OnlyCare("nobar")
	public void handleNoBar() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(window.getWidth(),window.getHeight());
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				getAnalyzer().process(content);
			}
		});
	}
	
	@CommandName("cls")
	public void clearCommandWindow() {
		window.clearCommandWindow();
	}
	
	/**
	 * 这就是所谓的自动参数类型转换，可将用户的合法输入自动转换为int型方便使用
	 * @param height
	 */
	@CommandName("height")
	public void setWindowHeight(int height) {
		window.setCommandHeight(height);
	}
	
	@CommandName("font")
	public void setFontSize(int size) {
		window.setFontSize(size);
	}
	
	@CommandName
	public void handleHint(String arg) {
		window.setHint(arg);
	}
	
	/**
	 * 布尔值类型的参数也可自动类型转换 当用户输入true/false时会自动转换为布尔类型
	 * @param can
	 */
	@CommandName("close")
	public void setWindowDefaultCloseOperation(boolean can) {
		if(can) {
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.out.println("设置成功 可通过鼠标关闭窗口");
		}else {
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			System.out.println("设置成功 已禁止鼠标关闭窗口");
		}
	}
	
	@CommandName("bound")
	public void setWindowBounds(int w , int h) {
		window.setBounds(w, h);
	}
	
	@CommandName
	public void handleHelp() {
		window.printlnSmoothly(getAnalyzer().getCommandsDescription());
	}
	
	/**
	 * 这个处理函数跟上面的处理函数是为了处理重载命令（一参help跟无参help）的，巧的是这两个处理函数也是重载的
	 * 当然了处理函数不一定也同时重载，只要CommandName注解标注好，即可根据参数个数定位对应处理函数
	 * @param commandName
	 */
	@CommandName
	public void handleHelp(String commandName) {
		String result = getAnalyzer().getCommandInfo(commandName);
		window.printlnSmoothly(result);
	}

	public void registerCommands(Analysable analysable) {

		analysable.addCommand(new Command("720p",	"设置窗口尺寸为720P"));
		analysable.addCommand(new Command("cls",	"清屏"));
		analysable.addCommand(new Command("exit",	"退出"));
		analysable.addCommand(new Command("help",	"帮助"));
		analysable.addCommand(new Command("version","版本号"));

		analysable.addCommand(new Command("help",	1,	"查看每个命令的详细信息"));
		analysable.addCommand(new Command("height",	1,	"设置窗口高度"));
		analysable.addCommand(new Command("font",	1,	"设置字体大小"));
		analysable.addCommand(new Command("close",	1,	"设置能否用鼠标关闭窗口"));
		analysable.addCommand(new Command("window",	1,	"max、full、nobar"));
		
		analysable.addCommand(new Command("bound",	2,	"设置窗口宽高"));
		
		analysable.addCommand(new Command("hint",	1,		"`",	"设置hint文字"));
	}
}