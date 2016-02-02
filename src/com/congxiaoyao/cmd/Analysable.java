package com.congxiaoyao.cmd;

import java.util.List;

/**
 * 实现此接口的类代表着拥有通过框架流程处理一条用户输入的能力
 * 代表着能够提供作为解析器的一些方法功能
 * 所谓框架流程，是指先将一条用户输入识别为一条指令并创建Command对象来维护这些信息，再通过这个这个对象去寻找相依的处理函数进行处理
 * 所以只要将用户输入传入{@code Analysable#process(String)}方法中，即可反射调用相应的处理函数完成处理
 * 其他方法用途见方法注释
 * 
 * @see CommandAnalyzer
 * @see CommandAnalyzerManager
 * 
 * @version　1.0
 * @author congxiaoyao
 * @date 2016.1.24
 */
public interface Analysable {
	
	/**
	 * 解析一条用户输入并调用相应处理函数处理之
	 * @param content 用户输入
	 * @return 处理成功true 否则false
	 */
	public abstract boolean process(String content);
	
	/**
	 * @return 命令的集合
	 */
	public abstract List<Command> getCommands();

	/**
	 * 向命令集合中添加一条命令
	 * @param command
	 */
	public abstract void addCommand(Command command);

	/**
	 * @return 所有命令的描述信息整理成String的形式返回
	 */
	public abstract String getCommandsDescription();

	/**
	 * 通过命令名获取这条命令的信息以String的形式返回
	 * @param commandName 所有的命令名为commandName的命令都会被找到
	 * @return 每一行分别是 commandName、paramCount、delimiter、handlingMethod
	 */
	public abstract String getCommandInfo(String commandName);
}