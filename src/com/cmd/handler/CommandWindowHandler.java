package com.cmd.handler;

import javax.swing.JFrame;

import com.cmd.annotations.CommandName;
import com.cmd.annotations.OnlyCare;
import com.cmd.annotations.Outline;
import com.cmd.core.Analysable;
import com.cmd.extras.CodeAssistant;
import com.cmd.extras.CommandWindow;

/**
 * 主要为了处理对CommandWindow的操作的命令，如清屏、退出、设置窗口大小提示语等
 * 正常的话这些处理函数应该耦合在CommandWindow所在的类中，这里主要是当做对cmd框架使用的一个简单demo
 *
 * 支持的命令有
 * '720p'			'设置窗口尺寸为720P'
 * 'cls'			'清屏'
 * 'exit'			'退出'
 * 'version'		'版本号'
 * 'height'			'设置窗口高度'
 * 'font'			'设置字体大小'
 * 'close'			'设置能否用鼠标关闭窗口'
 * 'window'			'max、full、nobar'
 * 'bound'			'设置窗口宽高'
 * 'hint'			'设置hint文字'
 * 
 * @author congxiaoyao
 * @date 2016.2.2
 * @version 2.0
 */

@Outline(commandNames = {"720p","cls", "exit","version", "height","font", "close","window", "bound","hint","ecc"},
         outlines = {"设置窗口尺寸为720P","清屏", "退出","版本号", "设置窗口高度","设置字体大小",
                     "设置能否用鼠标关闭窗口","max、full、nobar", "设置窗口宽高","设置hint文字","代码提示"})
public class CommandWindowHandler extends BaseHandler {
	
	private CommandWindow window;

	public CommandWindowHandler(CommandWindow window, Analysable analysable) {
		super(analysable);
		this.window = window;
	}

	@CommandName("restart")
	public void restartWindow() {
		window.closeWindow();
		window = new CommandWindow().setVisible();
		window.setOnSubmitListener(content -> getAnalysable().process(content));
	}

	/**
	 * CommandName注解没有括号也是可以的
	 */
	@CommandName
	public void exit() {
		window.closeWindow();
	}

	@CommandName
	public void version() {
		System.out.println("v2.0");
	}

	@CommandName("720p")
	public void handle720P() {
		getAnalysable().process("bound 1280 720");
		getAnalysable().process("font 20");
	}

	/**
	 * 这就是所谓的参数拦截特性，拦截的是window命令的max参数
	 */
    @OnlyCare("max")
	@CommandName("window")
	public void maxSizeWindow() {
		window.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

    @OnlyCare("full")
	@CommandName("window")
	public void handleFull() {
        int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(true);
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
		window.setOnSubmitListener(content -> getAnalysable().process(content));
	}

	@OnlyCare("nobar")
	@CommandName("window")
	public void handleNoBar() {
		int size = window.getFontSize();
		window.closeWindow();
		window = new CommandWindow(window.getWidth(),window.getHeight());
		window.setFontSize(size);
		window.setUndecorated(true);
		window.setVisible();
        window.setOnSubmitListener((content -> getAnalysable().process(content)));
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
	
	/**
	 * 布尔值类型的参数也可自动类型转换 当用户输入true/false时会自动转换为布尔类型
	 * @param can
	 */
	@CommandName("close")
	public void setWindowCloseOperation(boolean can) {
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
	public void hint(String arg) {
		window.setHint(arg);
	}

	@CommandName("ecc")
	public void enableCodeCompletion(boolean enable) {
		if (enable) {
			window.setAssistant(new CodeAssistant(getAnalysable()));
		}else {
			window.setAssistant(null);
		}
	}

}
