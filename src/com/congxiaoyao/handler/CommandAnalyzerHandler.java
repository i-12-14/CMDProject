package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提供了CommandAnalyzerManager中维护的CommandAnalyzer的信息的读取操作
 * 通过反射从外部拿到了CommandAnalyzer中的methodsMap、invoker、commandDirectory对象的信息并展示出来
 * 对于调试框架内程序十分有帮助
 *
 * 支持的命令有
 * 'ca_selc_id'     '1'     '通过id选出一个CommandAnalyzer从而得以执行ca_show'
 * 'ca_selc_name'   '1'     '通过类名(可简写)选出一个CommandAnalyzer从而得以执行ca_show'
 * 'ca_show'        '1'     '参数可选 methodsMap、invoker、directory、commands'
 * 'ca_reload'              '重新加载选中的CommandAnalyzer'
 * 'ca_ids'                 '所有的CommandAnalyzer的id'
 * 'ca_names'               '所有的CommandAnalyzer的invoker的ClassName'
 * 'ca_infos'               'ids+names'
 *
 * Created by congxiaoyao on 2016/2/12.
 * @version 1.0
 */
public class CommandAnalyzerHandler extends CommandHandler{

    private Set<CommandAnalyzer> set;
    private CommandAnalyzer nowAnalyzer;

    public CommandAnalyzerHandler() {
        set = CommandAnalyzerManager.getInstance().getAnalyzers();
    }
    public CommandAnalyzerHandler(CommandAnalyzer analyzer) {
        set = new HashSet<>(1);
        set.add(analyzer);
    }

    @OnlyCare("methodsMap")
    @CommandName("ca_show")
    public void showMethodsMap() {
        if (!checkNowAnalyzer()) return;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("methodsMap");
            field.setAccessible(true);
            Map<String, Method> methodsMap = (Map<String, Method>) field.get(nowAnalyzer);
            methodsMap.forEach((String s, Method method) ->{
                String[] methodInfo= method.toString().split(" ");
                String methodName = methodInfo[methodInfo.length - 1];
                int end = methodName.indexOf('(');
                methodName = methodName.substring(0, end);
                String[] split = methodName.split("\\.");
                String methodNameNew = split[split.length - 1];
                methodNameNew = method.toString().replace(methodName,methodNameNew);
                System.out.println("<" + s + " , " + methodNameNew + ">");
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    @OnlyCare("directory")
    @CommandName("ca_show")
    public void showCommandsDirectory() {
        if (!checkNowAnalyzer()) return;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("commandsDirectory");
            field.setAccessible(true);
            Map<Character, int[]> directory = (Map<Character, int[]>) field.get(nowAnalyzer);
            directory.forEach((character, ints) -> {
                System.out.println("<" + character + " , " + "start=" + ints[0] + "\tlen=" + ints[1] + ">");
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @OnlyCare("invoker")
    @CommandName("ca_show")
    public void showInvoker() {
        if (!checkNowAnalyzer()) return;
        try {
            System.out.println(getInvoker(nowAnalyzer).toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @OnlyCare("commands")
    @CommandName("ca_show")
    public void showCommands() {
        if (!checkNowAnalyzer()) return;
        List<Command> commands = nowAnalyzer.getCommands();
        for (Command command : commands) {
            String info = nowAnalyzer.getCommandInfo(command.commandName);
            if(info.contains("handlingMethod")) {
                System.out.print(nowAnalyzer.getCommandInfo(command.commandName));
            }
        }
    }

    @CommandName("ca_selc_id")
    public void selectCommandAnalyzerById(int id) {
        for (CommandAnalyzer analyzer : set) {
            if (analyzer.getId() == id) {
                nowAnalyzer = analyzer;
                System.out.println("successed!");
                return;
            }
        }
        System.out.println("error");
    }

    @CommandName("ca_selc_name")
    public void selectCommandAnalyzerByName(String className) {
        for (CommandAnalyzer analyzer : set) {
            try {
                Object invoker = getInvoker(analyzer);
                if (invoker.getClass().getName().contains(className)) {
                    nowAnalyzer = analyzer;
                    System.out.println("select " + invoker.getClass().getName()+" successed!");
                    return;
                }
            } catch (NoSuchFieldException e) {
                System.out.println("error NoSuchFieldException");
            } catch (IllegalAccessException e) {
                System.out.println("error IllegalAccessException");
            }
        }
        System.out.println("failed");
    }

    @CommandName("ca_ids")
    public void getCommandAnalyzerIds() {
        for (CommandAnalyzer analyzer : set) {
            System.out.println(analyzer.getId()+"");
        }
    }

    @CommandName("ca_names")
    public void getCommandAnalyzerNames() {
        for (CommandAnalyzer analyzer : set) {
            try {
                System.out.println(getInvoker(analyzer).getClass().getName());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @CommandName("ca_infos")
    public void getCommandAnalyzerInfos() {
        System.out.println("ids\t\tnames");
        for (CommandAnalyzer analyzer : set) {
            try {
                System.out.print(analyzer.getId()+"\t\t");
                System.out.println(getInvoker(analyzer).getClass().getSimpleName());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @CommandName("ca_reload")
    public void reload() {
        if (nowAnalyzer == null) {
            System.out.println("请先调用ca_selc_id或ca_selc_id命令通过选择一个CommandAnalyzer！");
            return;
        }
        try {
            Object invoker = getInvoker(nowAnalyzer);
            CommandManagementHandler handler = new CommandManagementHandler();
            handler.removeAnalyzer(nowAnalyzer.getId());
            handler.addHandlingMethod(invoker.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 通过检查返回true，没有返回false
     */
    private boolean checkNowAnalyzer() {
        if (nowAnalyzer == null) {
            System.out.println("请先调用ca_selc_id或ca_selc_name命令通过选择一个CommandAnalyzer！");
            return false;
        }
        return true;
    }

    /**
     * 通过反射获取CommandAnalyzer内的invoker对象
     * @param analyzer
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getInvoker(CommandAnalyzer analyzer) throws NoSuchFieldException, IllegalAccessException {
        Field field = CommandAnalyzer.class.getDeclaredField("invoker");
        field.setAccessible(true);
        return field.get(analyzer);
    }

    /**
     * 如果不通过文件配置命令的话，也可以调用这个函数动态的添加这些命令
     * 这个函数添加的所有命令的处理方法已经被CommandAnalyzerHandler中的处理函数实现了
     */
    @Override
    public CommandHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("ca_selc_id",
                1, "通过id选出一个CommandAnalyzer从而得以执行ca_show"));
        analysable.addCommand(new Command("ca_selc_name",
                1, "通过类名(可简写)选出一个CommandAnalyzer从而得以执行ca_show"));
        //输出选出的CommandAnalyzer的methodsMap/invoker/directory/commands的信息
        analysable.addCommand(new Command("ca_show", 1, "参数可选 methodsMap、invoker、directory、commands"));
        analysable.addCommand(new Command("ca_reload" , "重新加载选中的CommandAnalyzer"));
        analysable.addCommand(new Command("ca_ids" , "所有的CommandAnalyzer的id"));
        analysable.addCommand(new Command("ca_names" , "所有的CommandAnalyzer的invoker的ClassName"));
        analysable.addCommand(new Command("ca_infos", "ids+names"));
        return this;
    }
}