package com.congxiaoyao;

import com.congxiaoyao.cmd.Analysable;
import com.congxiaoyao.cmd.CommandAnalyzerManager;
import com.congxiaoyao.cmd.CommandWindow;
import com.congxiaoyao.cmd.CommandWindow.OnSubmitListener;
import com.congxiaoyao.cmd.CommandWindowHandler;

public class MainClass {

	private static Analysable analyzer;
	private static CommandWindow window;
	
	public static void main(String[] args) {
		window = new CommandWindow().setVisible();
		window.setOnSubmitListener(new OnSubmitListener() {
			@Override
			public void onSubmit(String content) {
				analyzer.process(content);
			}
		});
		CommandWindowHandler handler = new CommandWindowHandler(window);
		analyzer = CommandAnalyzerManager.handleWith(handler);
		handler.setAnalyzer(analyzer);
	}
}
