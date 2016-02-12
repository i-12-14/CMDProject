package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.io.File;

/**
 * 基于Analysable的getCommandsDescription、getCommandInfo方法提供帮助命令
 * 基于CommandAnalyzer的动态性特性实现了一些处理函数使得使用者可以在运行时对命令及其处理函数做增删操作
 * 
 * 支持的命令有
 * 'help'			'帮助'
 * 'help'	        '1'		'查看每个命令的详细信息'
 * 'remana'         '1'     '通过id移除一个CommandAnalyzer'
 * 'handle_with'    '1'     '参数为完整的类名 如com.cmd.Command'
 * 'addcmd'   '4'   '`'     '添加命令`命令名`参数个数`分隔符`描述'
 * 'addcmd'   '4'   ' '     '添加命令:命令名`参数个数`分隔符`描述'
 * 'delcmd'   '3'   '`'     '删除命令`命令名`参数个数`分隔符'
 * 'delcmd'   '3'   ' '     '删除命令:命令名`参数个数`分隔符'
 * 
 * Created by congxiaoyao on 2016/2/12.
 * @version 1.0
 */
public class CommandManagementHandler extends CommandHandler{

    /**
     * 可以在运行时添加一条命令，使得Analysable可以维护这条command
     * @param commandName
     * @param paramCount
     * @param delimiter
     * @param description
     */
    @CommandName("addcmd")
    public void addCommand(String commandName, int paramCount, String delimiter, String description) {
        getAnalysable().addCommand(new Command(commandName, paramCount, delimiter, description));
        System.out.println("ok");
    }

    /**
     * 在运行时通过这条命令删除一条命令
     * 注意，如果通过这条命令删除了自己，在不采取措施的情况下将无法再删除其他命令了
     * @param commandName
     * @param paramCount
     * @param delimiter
     */
    @CommandName("delcmd")
    public void deleteCommand(String commandName, int paramCount, String delimiter) {
        getAnalysable().removeCommand(new Command(commandName, paramCount, delimiter, ""));
        System.out.println("ok");
    }

    /**
     * 在运行时添加处理函数，如果通过addcmd添加了一条命令，就可以再通过这个函数添加他的处理函数
     * 按照运行前的方式编写一个处理函数编译后将处理函数所在类的类名(全名)作为命令参数传入即可
     * 最好是将新的处理函数写入新的类中，否则可能运行不稳定
     * @param className
     * @throws Exception
     */
    @CommandName("handle_with")
    public void addHandlingMethod(String className) throws Exception {
        String classPath = new File("bin").getAbsolutePath();
        DynamicClassLoader classLoader =
                new DynamicClassLoader(DynamicClassLoader.class.getClassLoader());
        Class<?> objectClass = classLoader.loadClass(classPath, className);
        Object handlingObject = objectClass.newInstance();
        CommandAnalyzerManager.handleWith(handlingObject);
        System.out.println("ok");
    }

    /**
     * 在运行时通过id删除一个CommandAnalyzer，使得其所维护的所有处理函数失效
     * 方法addHandlingMethod中提到最好将新的处理函数写入新的类中，否则可能运行不稳定
     * 如果执意要将新的处理函数写入旧的类中或改变旧的类中的处理函数的函数体，可以先通过此命令删除旧的analyzer
     * @param id
     */
    @CommandName("remana")
    public void removeAnalyzer(int id) {
        CommandAnalyzerManager.getInstance().removeHandlingObject(id);
        System.out.println("ok");
    }

    /**
     * 输出帮助信息
     */
    @CommandName
    public void handleHelp() {
        System.out.println("\5" + getAnalysable().getCommandsDescription());
    }

    /**
     * 输出某个特定指令的详细帮助信息 传入-all为输出全部命令的详细帮助信息
     * 强调下框架特性 这个处理函数跟上面的处理函分别处理了重载命令（一参help跟无参help）
     * 巧的是这两个处理函数也是重载的 当然了处理函数不一定也同时重载
     * 只要CommandName注解标注好，即可根据参数个数定位对应处理函数
     * @param commandName
     */
    @CommandName
    public void handleHelp(String commandName) {
        System.out.println("\5" + getAnalysable().getCommandInfo(commandName));
    }

    /**
     * 如果不通过文件配置命令的话，也可以调用这个函数动态的添加这些命令
     * 这个函数添加的所有命令的处理方法已经被CommandHandler中的处理函数实现了
     */
    @Override
    public CommandHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("help",	"帮助"));
        analysable.addCommand(new Command("help",	     1,	"查看每个命令的详细信息"));
        analysable.addCommand(new Command("remana",      1, "通过id移除一个CommandAnalyzer"));
        analysable.addCommand(new Command("handle_with", 1, "参数为完整的类名 如com.cmd.Command"));
        analysable.addCommand(new Command("addcmd", 4, "`", "添加命令`命令名`参数个数`分隔符`描述"));
        analysable.addCommand(new Command("addcmd", 4, " ", "添加命令:命令名`参数个数`分隔符`描述"));
        analysable.addCommand(new Command("delcmd", 3, "`", "删除命令`命令名`参数个数`分隔符"));
        analysable.addCommand(new Command("delcmd", 3, " ", "删除命令:命令名`参数个数`分隔符"));
        return this;
    }
}