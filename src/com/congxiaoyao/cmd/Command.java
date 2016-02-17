package com.congxiaoyao.cmd;

import java.util.Arrays;

/**
 * 请在cmd文件夹下配置values.cmd文件，配置好的所有的命令以这个对象的形式来进行管理
 * 或者通过构造方法构造出来也可以，通过一个静态的工厂方法？也可以构造出来
 * @see #forName(String)
 *
 * @author congxiaoyao
 * @date 2016.1.19
 * @version 1.4.1
 */

public class Command
{
	public String commandName;
	public int paramCount;
	public String delimiter;
	public String description;
	public String[] parameters;

	/**
	 * 这里强制规定了一条命令的格式 形如：[commandName][delimiter][parameter][delimiter][parameter]...
	 * @param commandName 命令头
	 * @param paramCount 参数个数
	 * @param delimiter 分隔符
	 * @param description 关于这条命令的注释，也就是帮助信息
	 */
	public Command(String commandName, int paramCount, String delimiter, String description) {
		this.commandName = commandName;
		this.paramCount = paramCount;
		this.delimiter = delimiter;
		this.description = description;
	}

	public Command(String commandName , int paramCount , String description){
		this(commandName,paramCount," ",description);
	}

	public Command(String commandName ,  String description){
		this(commandName, -1, description);
	}
	
	public Command(String commandName) {
		this(commandName, ".");
	}

	@Override
	public String toString() {
		return "Command{" +
				"commandName='" + commandName + '\'' +
				", paramCount=" + paramCount +
				", delimiter='" + delimiter + '\'' +
				", description='" + description + '\'' +
				", parameters=" + Arrays.toString(parameters) +
				'}';
	}

	/**
	 * 生成可以用来在文件中定义这个命令的字符串
	 */
	public String toDefinitionString() {
		StringBuilder builder = new StringBuilder("'");
		builder.append(commandName).append("'\t'");
		builder.append(paramCount).append("'\t'");
		builder.append(delimiter).append("'\t'");
		builder.append(description).append("'");
		return builder.toString();
	}

    /**
     * @param commandName
     * @return 跟一参的构造函数构造出来的一样
     */
    public static Command forName(String commandName) {
        return new Command(commandName);
    }

    public Command setDelimiter(String delimiter) {
        this.delimiter = delimiter;
        return this;
    }

    public Command setDescription(String description) {
        this.description = description;
        return this;
    }

    public Command setParamCount(int paramCount) {
        this.paramCount = paramCount;
        return this;
    }

    @Override
	public boolean equals(Object obj) {
		if(obj instanceof Command) {
			Command command = (Command) obj;
			if(command.commandName.equals(commandName) 
					&&command.paramCount == paramCount && command.delimiter.equals(delimiter)) {
				return true;
			}
		}
		return false;
	}
}