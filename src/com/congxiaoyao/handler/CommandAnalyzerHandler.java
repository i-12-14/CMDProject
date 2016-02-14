package com.congxiaoyao.handler;

import com.congxiaoyao.cmd.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
 * @version 1.4
 */
public class CommandAnalyzerHandler extends BaseHandler {

    private Set<CommandAnalyzer> set;
    private CommandAnalyzer nowAnalyzer;

    public CommandAnalyzerHandler() {
        set = CommandAnalyzerManager.getInstance().getAnalyzers();
    }

    /**
     * @return 获取CommandAnalyzer的set中的所有CommandAnalyzer的id
     */
    public static int[] getCommandAnalyzerIds() {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        int[] ids = new int[set.size()];
        Iterator<CommandAnalyzer> iterator = null;
        for (int i = 0; i < ids.length; i++) {
            ids[i] = iterator.next().getId();
        }
        return ids;
    }

    /**
     * @return 获取CommandAnalyzer的set中的所有CommandAnalyzer的name
     */
    public static String[] getCommandAnalyzerNames() {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        int index = 0;
        String[] names = new String[set.size()];
        for (CommandAnalyzer analyzer : set) {
            names[index++] = getInvoker(analyzer).getClass().getName();
        }
        return names;
    }

    /**
     * @param analyzer
     * @return CommandAnalyzer中的methodsMap对象
     */
    public static Map<String, Method> getMethodsMap(CommandAnalyzer analyzer) {
        Map<String, Method> methodsMap = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("methodsMap");
            field.setAccessible(true);
            methodsMap = (Map<String, Method>) field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return methodsMap;
    }

    /**
     * @param analyzer
     * @return CommandAnalyzer中的commandsDirectory对象
     */
    public static Map<Character, int[]> getCommandsDirectory(CommandAnalyzer analyzer) {
        Map<Character, int[]> directory = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("commandsDirectory");
            field.setAccessible(true);
            directory = (Map<Character, int[]>) field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return directory;
    }

    /**
     * @param analyzer
     * @return 通过反射获取CommandAnalyzer内的invoker对象
     */
    public static Object getInvoker(CommandAnalyzer analyzer){
        Object invoker = null;
        try {
            Field field = CommandAnalyzer.class.getDeclaredField("invoker");
            field.setAccessible(true);
            invoker = field.get(analyzer);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return invoker;
    }

    /**
     * @param analyzer
     * @return 参数analyzer所能处理的命令的集合
     */
    public static List<Command> getCommandsHandleBy(CommandAnalyzer analyzer) {
        List<Command> result = new ArrayList<>();
        List<Command> commands = analyzer.getCommands();
        for (Command command : commands) {
            String info = analyzer.getCommandInfo(command.commandName);
            if(info.contains("handlingMethod")) {
                result.add(command);
            }
        }
        return result;
    }

    /**
     * @param className 类名 全名简写皆可
     * @return 通过invoker的类名来寻找对应的CommandAnalyzer
     */
    public static CommandAnalyzer getCommandAnalyzerByName(String className) {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        for (CommandAnalyzer analyzer : set) {
            Object invoker = getInvoker(analyzer);
            if (invoker.getClass().getName().contains(className)) {
                return analyzer;
            }
        }
        return null;
    }

    /**
     * @param id CommandAnalyzer的id
     * @return 通过CommandAnalyzer的id来寻找对应的CommandAnalyzer
     */
    public static CommandAnalyzer getCommandAnalyzerById(int id) {
        Set<CommandAnalyzer> set = CommandAnalyzerManager.getInstance().getAnalyzers();
        for (CommandAnalyzer analyzer : set) {
            if (analyzer.getId() == id) {
                return analyzer;
            }
        }
        return null;
    }

    @CommandName("ca_show")
    public void showMethodsMap(@OnlyCare("methodsMap")String arg) {
        if (!checkNowAnalyzer()) return;
        Map<String, Method> methodsMap = getMethodsMap(nowAnalyzer);
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
    }


    @CommandName("ca_show")
    public void showCommandsDirectory(@OnlyCare("directory")String arg) {
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


    @CommandName("ca_show")
    public void showInvoker(@OnlyCare("invoker")String invoker) {
        if (!checkNowAnalyzer()) return;
        System.out.println(getInvoker(nowAnalyzer).toString());
    }


    @CommandName("ca")
    public void showCommands(@OnlyCare("commands")String arg) {
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
            Object invoker = getInvoker(analyzer);
            if (invoker.getClass().getName().contains(className)) {
                nowAnalyzer = analyzer;
                System.out.println("select " + invoker.getClass().getName()+" successed!");
                return;
            }
        }
        System.out.println("failed");
    }

    @CommandName("ca_ids")
    public void showCommandAnalyzerIds() {
        int[] ids = getCommandAnalyzerIds();
        for (int id : ids) {
            System.out.println(id + "");
        }
    }

    @CommandName("ca_names")
    public void showCommandAnalyzerNames() {
        String[] names = getCommandAnalyzerNames();
        for (String name : names) {
            System.out.println(name);
        }
    }

    @CommandName("ca_infos")
    public void showCommandAnalyzerInfos() {
        System.out.println("ids\t\tsize\t\tnames");
        for (CommandAnalyzer analyzer : set) {
            System.out.print(analyzer.getId()+"\t\t");
            System.out.print(analyzer.getHandlingMethodSize()+"\t\t");
            System.out.println(getInvoker(analyzer).getClass().getSimpleName());
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
     * 如果不通过文件配置命令的话，也可以调用这个函数动态的添加这些命令
     * 这个函数添加的所有命令的处理方法已经被CommandAnalyzerHandler中的处理函数实现了
     */
    @Override
    public BaseHandler registerCommands() {
        Analysable analysable = getAnalysable();
        analysable.addCommand(new Command("ca_selc_id",
                1, "通过id选出一个CommandAnalyzer从而得以执行ca_show"));
        analysable.addCommand(new Command("ca_selc_name",
                1, "通过类名(可简写)选出一个CommandAnalyzer从而得以执行ca_show"));
        //输出选出的CommandAnalyzer的methodsMap/invoker/directory/commands的信息
        analysable.addCommand(new Command("ca", 1, "参数可选 methodsMap、invoker、directory、commands"));
        analysable.addCommand(new Command("ca_reload" , "重新加载选中的CommandAnalyzer"));
        analysable.addCommand(new Command("ca_ids" , "所有的CommandAnalyzer的id"));
        analysable.addCommand(new Command("ca_names" , "所有的CommandAnalyzer的invoker的ClassName"));
        analysable.addCommand(new Command("ca_infos", "ids+names"));
        return this;
    }
}