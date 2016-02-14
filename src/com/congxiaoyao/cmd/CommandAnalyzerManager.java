package com.congxiaoyao.cmd;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * 为了解决处理函数的多类分布的问题，使用单例的方式通过CommandAnalyzerManager来调度所有的处理函数处理用户输入
 * 带来的好处是使得处理函数可以更好的跟相关的类内聚起来，降低类间耦合
 * 克服了一个CommandAnalyzer只能接受一个类中的处理函数的缺点，使得框架更加强大易用
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.1.21
 */

public class CommandAnalyzerManager implements Analysable{
	
	private static CommandAnalyzerManager commandManager = null;
	private Set<CommandAnalyzer> analyzers = null;
	private CommandAnalyzer lastAnalyzer = null;
	private int id = -1;

	/**
	 * 单例模式，获取CommandManager的实例
	 * @param handlingObject 含有处理函数的类的实例
	 * @return CommandManager
	 */
	public static CommandAnalyzerManager handleWith(Object handlingObject) {
		CommandAnalyzerManager manager = getInstance();
		synchronized (manager) {
			manager.addHandlingObject(handlingObject);
		}
		return manager;
	}
	
	/**
	 * @return 单例模式，获取CommandManager的实例
	 */
	public static CommandAnalyzerManager getInstance() {
		if(commandManager == null) {
			synchronized (CommandAnalyzerManager.class) {
				if(commandManager == null)
					commandManager = new CommandAnalyzerManager();
			}
		}
		return commandManager;
	}
	
	public CommandAnalyzerManager() {
		analyzers = new HashSet<>();
	}
	
	/**
	 * 添加一个CommandAnalyzer，这个analyzer的处理函数位于handlingObject中
	 * @param handlingObject
	 */
	public void addHandlingObject(Object handlingObject) {
		List<Command> commands = getCommands();
		CommandAnalyzer analyzer = null;
		if (commands == null) {
			analyzer = new CommandAnalyzer(handlingObject);
		}else {
			analyzer = new CommandAnalyzer(handlingObject, commands,
					analyzers.iterator().next().commandsDirectory);
		}
		analyzer.setId(new Random().nextInt(100000));
		lastAnalyzer = analyzer;
		analyzers.add(analyzer);
	}

	/**
	 * 添加一个CommandAnalyzer,如果传入的analyzer的id为-1 将随机的为其生成一个id
	 * @param analyzer 所维护的命令的集合必须与类内其他CommandAnalyzer内所维护的命令集合相同(同一个对象)
	 *                 否则将添加不成功
	 * @return 添加成功返回true 否则false
     */
	public boolean addCommandAnalyzer(CommandAnalyzer analyzer) {
		if (analyzer == null) return false;
		if (analyzers.size() == 0) {
			analyzers.add(analyzer);
			if (analyzer.getId() == -1) {
				analyzer.setId(new Random().nextInt(100000));
			}
			return true;
		}
		if (analyzer.getCommands() == analyzers.iterator().next().getCommands()) {
			analyzers.add(analyzer);
			if (analyzer.getId() == -1) {
				analyzer.setId(new Random().nextInt(100000));
			}
			return true;
		}
		return false;
	}

	/**
	 * 每一次调用addHandlingObject都会新建一个analyzer并放入一个set中，这里就是根据他们的id来移除之
	 * @param id
     */
	public boolean removeHandlingObject(int id) {
		Iterator<CommandAnalyzer> iterator = analyzers.iterator();
		while (iterator.hasNext()) {
			CommandAnalyzer analyzer = iterator.next();
			if (analyzer.getId() == id) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	public void removeLastHandlingObeject() {
		analyzers.remove(lastAnalyzer);
	}

	/**
	 * @return 命令集合
	 */
	@Override
	public List<Command> getCommands() {
		if(analyzers.size() == 0) return null;
		Iterator<CommandAnalyzer> iterator = analyzers.iterator();
		CommandAnalyzer analyzer = iterator.next();
		return analyzer.getCommands();
	}
	
	/**
	 * @return 命令字典，能稍微的提高一下查找效率
	 */
	public Map<Character, int[]> getCommandsDirectory(){
		if(analyzers.size() == 0) return null;
		Iterator<CommandAnalyzer> iterator = analyzers.iterator();
		CommandAnalyzer analyzer = iterator.next();
		return analyzer.commandsDirectory;
	}
	
	/**
	 * 添加一条命令，如果CommandManager中未配置CommandAnalyzer，函数将失效
	 * @param command
	 */
	@Override
	public void addCommand(Command command) {
		if(analyzers.size() == 0) return;
		analyzers.iterator().next().addCommand(command);
	}
	
	@Override
	public void removeCommand(Command command) {
		if(analyzers.size() == 0) return;
		analyzers.iterator().next().removeCommand(command);
	}
	
	/**
	 * 处理一条用户输入的内容，将内容指派到维护的所有的CommandAnalyzer中去尝试处理，直到有人能够处理他
	 * @param content 待识别的指令
	 */
	@Override
	public boolean process(String content) {
		if(analyzers.size() == 0) return false;
		Command command = analyzers.iterator().next().analyze(content);
		if(command != null) {
			for (CommandAnalyzer commandAnalyzer : analyzers) {
				if(commandAnalyzer.handleCommand(command)) return true;
			}
		}
		return false;
	}
	
	/**
	 * @return 所有的命令的描述拼接成一个string的形式返回
	 */
	@Override
	public String getCommandsDescription() {
		for (CommandAnalyzer commandAnalyzer : analyzers) {
			return commandAnalyzer.getCommandsDescription();
		}
		return null;
	}

	@Override
	public String getCommandInfo(String commandName) {
		StringBuilder builder = new StringBuilder();
		List<Command> selected = getCommandsByName(commandName, getCommands(),getCommandsDirectory());
		for (Command command : selected) {
			builder.append('\n');
			CmdUtils.appendCommandAttribute(command, builder);
			for (CommandAnalyzer analyzer : analyzers) {
				Method method = analyzer.methodsMap.get(command.commandName+
						(command.paramCount == -1 ? 
								(command.parameters == null ? 0 : command.parameters.length)
										: command.paramCount));
				if(method == null) method = analyzer.methodsMap.get(command.commandName + "$");
				if(method != null) {
					builder .append("handlingMethod-->")
							.append(CmdUtils.getSimpleMethodSignature(method.toGenericString()))
							.append('\n');
					builder .append("analyzer_id-->").append(analyzer.getId()).append('\n');
					break;
				}
				//有可能这个command对应了好多个handlingMethod，一点一点找吧
				else {
					Set<Entry<String,Method>> entrySet = analyzer.methodsMap.entrySet();
					for (Entry<String, Method> entry : entrySet) {
						String key = entry.getKey();
						if(CmdUtils.isBeginWith(commandName, key, null)) {
							builder	.append("handlingMethod-->")
									.append(CmdUtils.getSimpleMethodSignature(entry.getValue().toGenericString()))
									.append('\n');
							builder	.append("analyzer_id-->").append(analyzer.getId()).append('\n');
						}
					}
				}
			}
		}
		if(builder.length() == 0) {
			if(commandName.equals("-all")) {
				List<Command> commands = getCommands();
				Set<String> commandNames = new HashSet<>(commands.size());
				for (Command command : commands) {
					commandNames.add(command.commandName);
				}
				for (String string : commandNames) {
					builder.append(getCommandInfo(string));
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	@Override
	public int getHandlingMethodSize() {
		int size = 0;
		for (CommandAnalyzer analyzer : analyzers) {
			size += analyzer.getHandlingMethodSize();
		}
		return size;
	}

	@Override
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<CommandAnalyzer> getAnalyzers() {
		return analyzers;
	}
	
	/**
	 * 将命令名为commandName的命令全都从commands中挑出来
	 * @param commandName
	 * @param commands
	 * @param directory
	 * @return
	 */
	public static List<Command> getCommandsByName(String commandName, List<Command> commands,
			Map<Character, int[]> directory) {
		List<Command> selected = new ArrayList<>(1);
		if (commandName == null || commandName.length() == 0)
			return null;

		char key = commandName.charAt(0);
		int[] info = directory.get(key);
		if (info != null) {
			for (int i = info[0], len = info[0] + info[1]; i < len; i++) {
				Command command = commands.get(i);
				if(command.commandName.equals(commandName))
					selected.add(commands.get(i));
			}
		}
		return selected;
	}
}