package com.cmd.core;

import com.cmd.annotations.*;
import com.cmd.utils.CmdUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 在这个类里解释一下这一整套所谓的框架的工作原理及使用方法
 * 我希望这套框架能够为实现一个命令提示符系统提供一些必要的支持，使得框架的使用者可以专注于处理事务
 * 从而不必在命令定义、分析、查找、执行等细节上花费太多时间
 * <p>
 * 首先明确两个概念，命令以及处理函数
 * <ul>
 * <li>一条命令由注解{@code CmdDef}定义，注解的使用方式见注解内注释
 * <li>当注解被绑定在一个函数上时，这个被绑定的函数被称作处理函数，代表着处理这条命令的函数
 * </ul>
 * 下面是使用范例
 * <hr><pre>
 * <code>@CmdDef(commandName = "test", delimiter = " ", description = "for test")</code>
 * public void test(String arg){
 *     //test函数即一参处理函数 当用户输入test XXX时，XXX将会通过参数arg传入函数
 * }
 * //当然，CmdDef中所有的参数都不是必须的，下面是简化版的定义方式
 * <code>@CmdDef</code>
 * public void handleTest(String arg){
 *     //commandName默认下通过函数名识别,这里为test delimiter默认就是空格 description在默认情况下以函数名代替
 * }
 * </pre><hr>
 * 除此之外，一条命令还可以由这三个注解来定义
 * <ul>
 * <li><code>@CommandName</code>
 * <li><code>@Delimiter</code>
 * <li><code>@Description</code>
 * </ul>
 * 实质上是将{@code CmdDef}中的参数拆解开来分别定义
 * 需要注意的是，这三个注解不能与{@code CmdDef}同时出现在同一个处理函数上，否则将导致这三个注解失效
 * <p>
 * 当绑定动作完成，即可使用本类处理一条输入，可以分为如下几步
 * <ul>
 * <li>单例模式通过{@code getInstance()}获取{@code CommandAnalyzer}实例
 * <li>通过{@code addHandlingObject(Object)}方法将处理函数所在的类的实例传入
 * <li>最后是需要调用{@code Analysable#process(String)}即可完成对一条输入的处理
 * </ul>
 *
 * <p>关于重载命令</p>
 * 由一个命令名及一个分隔符可以唯一确定一条命令，所以命令支持分隔符重载
 * 同时命令支持不同参数个数的重载，对于同一个命令，可以绑定很多不同参数个数的处理函数
 *
 * <p>关于可变参数特性</p>
 * 可变参数特性是指在处理函数定义的时候并不关心准确的参数个数，任意的参数个数我都希望去处理，这时
 * 处理函数的参数需要定义为Command类型或String[]类型且不能再有其他参数，且不能带有OnlyCare注解
 *
 * <p>关于OnlyCare</p>
 * 对于有参命令，我们可能仅仅关心某个参数中的某个特定值，通常下我们会通过分支语句来拦截这些特定值
 * 但我们可以通过OnlyCare注解来帮我们拦截敏感参数，他可以被注解在处理函数上或处理函数的参数前
 * 想要了解详细的使用方法详见{@code OnlyCare}类内注释
 * 需要说明的是，假设同时存在两个处理相同命令且参数个数相同的处理函数，其中一个带有OnlyCare注解，另一个不带，
 * CommandAnalyzer会优先尝试调用带有OnlyCare注解的处理函数
 *
 * <p>关于自动参数类型转换</p>
 * 一般来说，我们必须将处理函数的参数定义为String类型的
 * 但是如果命令中的参数是基本数据类型的一种 如两整数相加的命令，两个参数的类型实际是int型的
 * 那么允许将处理函数的参数类型定义为int或Integer类型，CommandAnalyzer会自动将String类型的参数转为int/Integer型
 *
 * <p>关于处理函数的多类分布</p>
 * 因为{@code CommandAnalyzer}是单例的，所以可以在任何地方获取{@code CommandAnalyzer}的实例
 * 从而将分布在不同类中的处理函数跟命令交给{@code CommandAnalyzer}来处理
 * 好处就是可以将处理函数耦合进不同的类中从而随心所欲不逾矩
 *
 * <p>关于动态特性</p>
 * 支持命令的动态删除，可以通过代码甚至是命令删除一条命令见{@code CommandAnalyzer#removeCommand(Command)}
 * 支持命令及处理函数的动态添加，通过{@code DynamicClassLoader}实现了class文件的热加载
 * 动态特性使得程序运行起来之后仍然可以动态的添加、删除命令甚至可以改变命令的处理方式
 * 这里只是留下关于动态特性的想象空间，并没有提供实际的接口，但理论上是完全可行的
 *
 * <p>其他</p>
 * 一般情况下命令是必须有分隔符的，即使不使用注解去标明，也会存在默认分隔符空格
 * 但对于一参及无参命令，允许无分隔符定义命令，只要将delemiter标为null即可
 * <p>
 * 如<code>@Description("null")</code>或<code>@CmdDef(description = "null")</code>
 * <p>
 * 我们提供了Outline注解用于阐明对于一个CommandName的总体性的概述，使用方式见类头注释
 *
 * @version 2.0
 * Created by congxiaoyao on 2016/2/19.
 */
public class CommandAnalyzer implements Analysable {

    private static CommandAnalyzer commandAnalyzer;

    private List<Command> commands;
    private Map<Character, int[]> commandsDirectory;
    private Map<String, String> outlineMap;
    private Map<Class<?>,StringPraser> typesMap;

    /**
     * @return 单例模式，获取CommandAnalyzer的实例
     */
    public static CommandAnalyzer getInstance() {
        if (commandAnalyzer == null) {
            synchronized (CommandAnalyzer.class) {
                if (commandAnalyzer == null)
                    commandAnalyzer = new CommandAnalyzer();
            }
        }
        return commandAnalyzer;
    }

    /**
     * 单例模式，获取CommandAnalyzer的实例并解析handlingObject中的命令与处理函数
     *
     * @param handlingObject
     * @return
     */
    public static CommandAnalyzer handleWith(Object handlingObject) {
        CommandAnalyzer analyzer = getInstance();
        synchronized (analyzer) {
            analyzer.addHandlingObject(handlingObject);
        }
        return analyzer;
    }

    private CommandAnalyzer() {
        commands = new ArrayList<>();
        commandsDirectory = new HashMap<>();
        outlineMap = new TreeMap<>();
        initTypesMap();
    }

    /**
     * 解析handlingObject中的命令与处理函数
     *
     * @param handlingObject 包含处理函数的对象
     */
    public CommandAnalyzer addHandlingObject(Object handlingObject) {
        Method[] methods = handlingObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //尝试根据method上的注解生成Command对象
            Command temp = getCommandByMethod(method);
            if (temp == null) continue;
            //赋值invoker以便反射调用
            temp.getHandlingMethods().get(0).invoker = handlingObject;
            //将command对象添加到command集合中，会查重
            boolean repeat = false;
            //遍历命令集合，处理新获取的Command对象已经存在的情况
            for (Command command : commands) {
                //如果命令集合中已经存在这个命令了
                if (command.equals(temp)) {
                    repeat = true;
                    //将新生成的Command对象中的handlingMethod添加到已经存在的command中
                    try {
                        command.addHandlingMethod(temp.getHandlingMethods().get(0));
                    } catch (IllegalHandlingMethodException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            //如果获取的Command对象不存在于命令集合，将其添加到命令结合
            if (!repeat) insertCommand(temp);
        }
        updateCommandsDirectory();
        sortHandlingMethods();
        //添加outline
        if (!handlingObject.getClass().isAnnotationPresent(Outline.class)) return this;
        Outline outline = handlingObject.getClass().getAnnotation(Outline.class);
        String[] commandNames = outline.commandNames();
        String[] outlines = outline.outlines();
        for (int i = 0; i < commandNames.length; i++) {
            outlineMap.put(commandNames[i], outlines[i]);
        }
        return this;
    }

    public void initTypesMap() {
        typesMap = new HashMap<>(14);
        typesMap.put(Boolean.class,(arg)->{
            if(arg.equals("true")) return true;
            else if (arg.equals("false")) return false;
            else return null;
        });
        typesMap.put(boolean.class,(arg)->{
            if(arg.equals("true")) return true;
            else if (arg.equals("false")) return false;
            else return null;
        });
        typesMap.put(Integer.class,(arg) -> Integer.parseInt(arg));
        typesMap.put(int.class,(arg) ->     Integer.parseInt(arg));
        typesMap.put(Double.class,(arg) -> Double.parseDouble(arg));
        typesMap.put(double.class,(arg) -> Double.parseDouble(arg));
        typesMap.put(Byte.class,(arg) -> Byte.parseByte(arg));
        typesMap.put(byte.class,(arg) -> Byte.parseByte(arg));
        typesMap.put(Float.class,(arg) -> Float.parseFloat(arg));
        typesMap.put(float.class,(arg) -> Float.parseFloat(arg));
        typesMap.put(Short.class,(arg) -> Short.parseShort(arg));
        typesMap.put(short.class,(arg) -> Short.parseShort(arg));
        typesMap.put(Long.class,(arg) -> Long.parseLong(arg));
        typesMap.put(String.class, (arg) -> arg);
    }

    /**
     * 尝试通过method上标记的注解来生成Command对象
     *
     * @param method
     * @return 获取失败返回null
     */
    private Command getCommandByMethod(Method method) {
        Command command = null;
        String description = Description.DEFAULT_VALUE;
        //只有两种情况可以通过注解生成Command对象
        //第一种情况是方法上标有CmdDef注解
        if (method.isAnnotationPresent(CmdDef.class)) {
            CmdDef cmdDef = method.getAnnotation(CmdDef.class);
            //如果cmdDef.commandName()是默认值将试图通过函数名解析
            String commandName = reanalyseCommandName(cmdDef.commandName(), method);
            if (commandName == null) return null;
            //如果分隔符是转义字符就给他转义
            String delimiter = CmdUtils.characterEscape(cmdDef.delimiter());
            command = new Command(commandName, delimiter);
            //如果cmdDef.description()是默认值则给description赋值函数名
            description = reanalyseDescription(cmdDef.description(), method);
        }
        //第二种情况是方法上至少标有CommandName注解
        else if (method.isAnnotationPresent(CommandName.class)) {
            CommandName annotation = method.getAnnotation(CommandName.class);
            //如果annotation.value()是默认值将试图通过函数名解析
            String commandName = reanalyseCommandName(annotation.value(), method);
            if (commandName == null) return null;
            String delimiter = " ";
            //如果存在方法上存在Delimiter注解，则delimiter为注解中的值
            if (method.isAnnotationPresent(Delimiter.class)) {
                //如果分隔符是转义字符就给他转义
                delimiter = method.getAnnotation(Delimiter.class).value();
                delimiter = CmdUtils.characterEscape(delimiter);
            }
            command = new Command(commandName, delimiter);
            //如果存在方法上存在Description注解，则Description为注解中的值
            if (method.isAnnotationPresent(Description.class)) {
                description = method.getAnnotation(Description.class).value();
            }
            //如果没有Description注解或Description注解为默认值则给description赋值函数名
            description = reanalyseDescription(description, method);
        }
        //其他情况将不被认为能解析出命令
        else return null;
        try {
            //由这个method对象生成handlingMethod
            HandlingMethod handlingMethod = new HandlingMethod(method);
            handlingMethod.setDescription(description);
            command.addHandlingMethod(handlingMethod);
        } catch (IllegalHandlingMethodException | BadDefinitionException e) {
            e.printStackTrace();
            return null;
        }
        return command;
    }

    /**
     * 对于从注解中获取的CommandName，可能是空字符，这里重新对这种情况进行分析并最终返回新的结果
     *
     * @param commandName
     * @param method
     * @return 如果传入的commandName是空字符且分析失败，将返回null
     * 分析失败是指无法通过函数名解析命令名(这条命令不是以handle开头)
     */
    private String reanalyseCommandName(String commandName, Method method) {
        if (commandName.length() == 0) {
            if (CmdUtils.isBeginWith("handle", method.getName(), null)) {
                commandName = method.getName().replaceFirst("handle", "").toLowerCase();
            } else {
                return null;
            }
        }
        return commandName;
    }

    /**
     * @param description
     * @param method
     * @return 对于从注解中获取的Description，可能是默认字符串，这里将其重新解析成方法名并返回
     */
    private String reanalyseDescription(String description, Method method) {
        if (description.equals(Description.DEFAULT_VALUE)) {
            description = CmdUtils.getMoreSimpleMethodSignature(method);
        }
        return description;
    }

    /**
     * 将一条command插入到commands集合中，但并不更新目录map
     * 一定不要忘记更新目录，否则会出问题
     *
     * @param command
     * @return 插入完成后参数command在命令集合中的位置，插入失败返回-1
     */
    private int insertCommand(Command command) {
        if (command == null) return -1;
        Character key = command.commandName.charAt(0);
        int[] info = commandsDirectory.get(key);
        if (info == null) {
            int start = commands.size();
            commands.add(command);
            commandsDirectory.put(key, new int[]{start, 1});
            return start;
        } else {
            //找位置插队
            for (int i = 0, len = commands.size(); i < len; i++) {
                if (commands.get(i).commandName.charAt(0) == key) {
                    commands.add(i, command);
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 更新命令目录，使得目录map能够正常工作
     */
    private void updateCommandsDirectory() {
        if (commands.size() <= 1) return;
        //根据排好的顺序建立目录
        Iterator<Command> iterator = commands.iterator();
        Command temp = iterator.next();
        char lastChar = temp.commandName.charAt(0), nowChar;
        int len = 1;
        for (int i = 1; iterator.hasNext(); i++) {
            temp = iterator.next();
            nowChar = temp.commandName.charAt(0);
            if (nowChar != lastChar) {
                int[] info = commandsDirectory.get(lastChar);
                info[0] = i - len;
                info[1] = len;
                len = 0;
            }
            lastChar = nowChar;
            len++;
        }
        int[] info = commandsDirectory.get(lastChar);
        info[0] = commands.size() - len;
        info[1] = len;
    }

    /**
     * 对于类内维护的Command集合中的所有的command，对其维护的HandlingMethod按OnlyCare数量排序
     */
    public void sortHandlingMethods() {
        for (Command command : commands) {
            command.sortHandlingMethod();
        }
    }

    /**
     * 分析一条字符串是否是一条给定的命令,要求commandName相同且分隔符没毛病
     *
     * @param content 用户提交的字符串
     * @return 如果能够成功匹配，返回一个command对象，否则返回null
     */
    public Command analyze(String content) {
        if (content.length() == 0) return null;
        int[] info = commandsDirectory.get(content.charAt(0));
        if (info == null) return null;
        for (int i = info[0], len = info[1] + i; i < len; i++) {
            Command command = commands.get(i);
            //对于一参无分隔符命令特别处理
            if (command.delimiter.equals("null") &&
                    CmdUtils.isBeginWith(command.commandName, content, null)) {
                String param = content.replaceFirst(command.commandName, "");
                //如果拿掉commandName后留下了一些内容
                if (param.length() != 0) {
                    command.parameters = new String[]{param};
                }else {
                    command.parameters = null;
                }
                return command;
            }
            //先看给定字符串是不是当前这个命令的类型
            if (!CmdUtils.isBeginWith(command.commandName, content, command.delimiter)) continue;
            //然后在去掉命令开头去分析参数
            String contentNew = content.replaceFirst(command.commandName, "");
            //初始化参数个数
            String[] parameters = null;
            //如果有参数，求给定字符串中包含的参数个数
            if (contentNew.length() > 1) {
                contentNew = contentNew.replaceFirst(command.delimiter, "");
                parameters = contentNew.split(command.delimiter);
                command.parameters = parameters;
                return command;
            }
            //没参数将command.parameters置为null
            else {
                command.parameters = null;
                return command;
            }
        }
        return null;
    }

    /**
     * command对象中去寻找对应的处理它的方法（依据Command参数个数、能否通过OnlyCare的筛查）
     *
     * @param command
     * @return 如果传入的command处理成功返回true否则返回false
     */
    public boolean handleCommand(Command command) throws NoneHandlingMethodException {
        List<HandlingMethod> handlingMethods = command.getHandlingMethods();
        if (handlingMethods.isEmpty())
            throw new NoneHandlingMethodException(command.toString());
        for (HandlingMethod handlingMethod : handlingMethods) {
            try {
                if (checkAndInvoke(command, handlingMethod)) {
                    return true;
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 检查参数handlingMethod是不是符合当前command的调用标准（这个处理函数是不是能够处理这个command）
     * 如果能够处理就去反射调用
     *
     * @param command
     * @param handlingMethod
     * @return 成功反射调用返回true
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private boolean checkAndInvoke(Command command, HandlingMethod handlingMethod)
            throws InvocationTargetException, IllegalAccessException {
        int mtdParCnt = handlingMethod.getParamCount();
        int cmdParCnt = command.parameters == null ? 0 : command.parameters.length;
        //如果handlingMethod的参数个数与command中所保存的参数个数吻合
        HandlingMethod.VariableType variableType = handlingMethod.variadicType;
        //检查处理函数上的参数是否为Command类型或String数组类型 如果是完成反射调用
        if (variableType != HandlingMethod.VariableType.TYPE_IMMUTABLE) {
            if (command.parameters == null) command.parameters = new String[0];
            if (variableType == HandlingMethod.VariableType.TYPE_COMMAND) {
                handlingMethod.method.invoke(handlingMethod.invoker, command);
                return true;
            } else {
                handlingMethod.method.invoke(handlingMethod.invoker, (Object) command.parameters);
                return true;
            }
        }
        //现在不存在可变参数的处理函数了，检查handlingMethod的参数个数与command中所保存的参数个数是否吻合
        if (mtdParCnt != cmdParCnt) return false;
        //检查OnlyCare是否能通过
        if (!checkIfOnlyCareCanPass(handlingMethod, command)) return false;
        //无参的处理函数不需要基本参数类型转换，已经可以反射调用了
        if (handlingMethod.method.getParameterCount() == 0) {
            handlingMethod.method.invoke(handlingMethod.invoker);
            return true;
        }
        //强制参数类型转换
        Class<?>[] types = handlingMethod.getParameterTypes();
        Object[] objects = new Object[types.length];
        try {
            for (int i = 0, len = objects.length; i < len; i++){
                objects[i] = toType(command.parameters[i], types[i]);
                if (objects[i] == null) return false;
            }
        } catch (IllegalHandlingMethodException e) {
            System.err.println(handlingMethod.method.toString());
            e.printStackTrace();
            return false;
        }
        //反射调用
        handlingMethod.method.invoke(handlingMethod.invoker, (Object[]) objects);
        return true;
    }

    /**
     * 判断这个method是否带有OnlyCare注解，如果带有则判断是否符合OnlyCare的要求
     *
     * @param command
     * @param method
     * @return 符合要求（通过了可以被反射调用）返回true
     */
    private static boolean checkIfOnlyCareCanPass(HandlingMethod method, Command command) {
        String[] params = command.parameters;
        if (!method.isOnlyCareAnnotated()) return true;
        for (int i = 0; i < method.getParamCount(); i++) {
            String care = method.getOnlyCareByParam(i);
            if (care != null && !params[i].equals(care)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将一个String类型的参数arg解析为type类型并以Object类型的形式返回
     *
     * @param arg
     * @param type
     * @return
     * @throws IllegalHandlingMethodException
     */
    private Object toType(String arg, Class<?> type) throws IllegalHandlingMethodException {
        StringPraser praser = typesMap.get(type);
        Object result = null;
        if(praser == null) throw new IllegalHandlingMethodException();
        try {
            result = praser.prase(arg);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    @Override
    public boolean process(String content) {
        Command command = analyze(content);
        if (command != null) {
            try {
                return handleCommand(command);
            } catch (NoneHandlingMethodException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public void removeCommand(Command command) {
        if (command == null) return;
        char key = command.commandName.charAt(0);
        int[] sl = commandsDirectory.get(key);
        int start = sl[0], end = sl[1] + start;
        for (int i = start; i < end; i++) {
            if (command.equals(commands.get(i))) {
                commands.remove(i);
                break;
            }
        }
        updateCommandsDirectory();
    }

    @Override
    public String getCommandsDescription() {
        StringBuilder builder = new StringBuilder();
        for (Command command : commands) {
            //准备一下关于这条命令的帮助信息
            String description = "too much..";
            List<HandlingMethod> handlingMethods = command.getHandlingMethods();
            String outline = outlineMap.get(command.commandName);
            if (outline != null) {
                description = outline;
            }else {
                if (handlingMethods.size() == 1) {
                    description = handlingMethods.get(0).getDescription();
                } else if (handlingMethods.isEmpty()) {
                    description = "null";
                }
            }
            //开始合成字符串
            builder.append(command.commandName);
            builder.append(command.commandName.length() > 7 ? "\t" : "\t\t");
//            builder.append(command.commandName.length() < 4 ? "\t" : "");
            builder.append(description);
            builder.append('\n');
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    @Override
    public String getCommandInfo(String commandName) {
        StringBuilder builder = new StringBuilder();
        for (Command command : commands) {
            if (!command.commandName.equals(commandName)) continue;
            builder.append("commandName-->").append(command.commandName).append('\n');
            builder.append("delimiter-->").append(command.delimiter).append('\n');
            String outline = outlineMap.get(commandName);
            if (outline != null) builder.append("outline-->").append(outline).append('\n');
            List<HandlingMethod> handlingMethods = command.getHandlingMethods();
            for (HandlingMethod handlingMethod : handlingMethods) {
                builder.append("handlingMethod-->").append(handlingMethod.method).append('\n');
                builder.append("description-->").append(handlingMethod.getDescription()).append('\n');
                if (handlingMethod.isOnlyCareAnnotated()) {
                    builder.append("OnlyCare-->");
                    for (int i = 0; i < handlingMethod.getOnlyCareCount(); i++) {
                        String care = null;
                        if ((care = handlingMethod.getOnlyCareByParam(i)) != null) {
                            builder.append("arg").append(i).append(' ').append(care).append(' ');
                        }
                    }
                    builder.append('\n');
                }
            }
        }
        if (builder.length() == 0) return "";
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    @Override
    public int getHandlingMethodSize() {
        int size = 0;
        for (Command command : commands) {
            size += command.getHandlingMethods().size();
        }
        return size;
    }

    public String getOutLine(String commandName) {
        return outlineMap.get(commandName);
    }
}
