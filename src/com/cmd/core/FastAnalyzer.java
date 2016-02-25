package com.cmd.core;

import com.cmd.annotations.Outline;

import java.lang.reflect.Method;
import java.util.TreeMap;

/**
 * Created by congxiaoyao on 2016/2/25.
 */
public class FastAnalyzer extends CommandAnalyzer{

    private static FastAnalyzer fastAnalyzer;

    private Node rootNode;

    private int realTreeHeight = 0;          //搜索树的高度，不包括rootNode那一层

    /**
     * @return 单例模式，获取CommandAnalyzer的实例
     */
    public static FastAnalyzer getInstance() {
        if (fastAnalyzer == null) {
            synchronized (CommandAnalyzer.class) {
                if (fastAnalyzer == null)
                    fastAnalyzer = new FastAnalyzer();
            }
        }
        return fastAnalyzer;
    }

    /**
     * 单例模式，获取CommandAnalyzer的实例并解析handlingObject中的命令与处理函数
     *
     * @param handlingObject
     * @return
     */
    public static FastAnalyzer handleWith(Object handlingObject) {
        FastAnalyzer analyzer = getInstance();
        analyzer.addHandlingObject(handlingObject);
        return analyzer;
    }

    private FastAnalyzer() {
        super(false);
        rootNode = new Node('\0');
        outlineMap = new TreeMap<>();
        initTypesMap();
    }

    /**
     * 将command对象添加到rootNode所维护的树中
     * @param command
     * @throws IllegalHandlingMethodException
     */
    private void addCommandToRootNode(Command command) throws IllegalHandlingMethodException {
        char[] content = command.commandName.toCharArray();
        Node node = rootNode;
        for (char c : content) {
            node = node.findOrAddNodeToNextLayer(c);
        }
        //如果最终的node中没有command对象直接添加进去
        if (node.commands == null) {
            node.addCommand(command);
            return;
        }
        //如果node中能找到相同的command 就只添加handlingMethod
        Command[] commands = node.commands;
        for (Command existed : commands) {
            if (existed.parameters.equals(command.parameters)) {
                existed.addHandlingMethod(command.getHandlingMethods().get(0));
                return;
            }
        }
        //如果找不到就直接添加进去
        node.addCommand(command);
    }

    @Override
    public CommandAnalyzer addHandlingObject(Object handlingObject) {
        Method[] methods = handlingObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            //尝试根据method上的注解生成Command对象
            Command temp = getCommandByMethod(method);
            if (temp == null) continue;
            //赋值invoker以便反射调用
            temp.getHandlingMethods().get(0).invoker = handlingObject;
            //将command对象添加到搜索树中
            try {
                addCommandToRootNode(temp);
            } catch (IllegalHandlingMethodException e) {
                e.printStackTrace();
            }
        }
        //计算生成树的高度,不包括rootNode那一层的节点
        realTreeHeight = calculateMaxTreeHeight(rootNode) - 1;
        //按OnlyCare个数给每个command里的handlingMethods排序
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
    /**
     * 对于类内维护的查找树中的所有的command，对其维护的HandlingMethods按OnlyCare数量排序
     */
    @Override
    public void sortHandlingMethods() {
        rootNode.iterateChild((command -> command.sortHandlingMethod()));
    }

    /**
     * @param node
     * @return 计算搜索树的层数，如果传入rootNode，返回的结果包括rootNode那一层
     */
    public int calculateMaxTreeHeight(Node node) {
        if (node.realLayerLen == 0) return 1;
        final int[] max = {-1};
        node.iterateNextLayer((nextLayerNode)->{
            int count = calculateMaxTreeHeight(nextLayerNode);
            if(count > max[0]) max[0] = count;
        });
        return max[0] + 1;
    }

    @Override
    public Command analyze(String content) {
        int len = content.length() > realTreeHeight ? realTreeHeight : content.length();
        char[] inputs = new char[len];
        content.getChars(0, len, inputs, 0);
        int nowIndex = -1;
        Node finder = new Node('\0');
        Node nowNode = rootNode;
        for (int i = 0; i < len; i++) {
            finder.c = inputs[i];
            if(nowNode.nextLayer == null) return null;
            nowNode = nowNode.findNodeInNextLayer(finder);
            if(nowNode == null) return null;
            nowIndex++;
            Command[] commands = nowNode.commands;
            if (nowNode.commands == null) continue;
            for (Command command : commands) {
                if (isDelimiterMatch(command.delimiter, content, nowIndex+1)) {
                    analyzeCommandParam(command, content);
                    return command;
                }
            }
        }
        return null;
    }

    /**
     * 给定用户输入，同时给定分隔符开始的位置，判断从分隔符开始后面是不是参数delimiter
     * @param delimiter 一个command对象中的delimiter
     * @param content 用户输入
     * @param startIndex 用户输入中疑似分隔符的起始位置
     * @return 如果成功匹配返回true
     */
    private boolean isDelimiterMatch(String delimiter, String content, int startIndex) {
        if (content.length() == startIndex) return true;
        if(delimiter.equals("null")) return true;
        int delimiterLen = delimiter.length();
        if (delimiterLen + startIndex > content.length()) return false;
        return delimiter.equals(content.substring(startIndex, startIndex + delimiterLen));
    }

    /**
     * 已经决定用户输入content是命令command，但在要根据command中的分隔符信息从content中解析出参数来
     * @param command
     * @param content
     */
    private void analyzeCommandParam(Command command, String content) {
        //无分隔符情况
        if (command.delimiter.equals("null")) {
            String param = content.substring(0, command.commandName.length());
            //如果拿掉commandName后留下了一些内容
            if (param.length() != 0) {
                command.parameters = new String[]{param};
            }else {
                command.parameters = null;
            }
        }
        //输入了一个无参命令
        else if (command.commandName.length() == content.length()) {
            command.parameters = null;
        }
        //通过分隔符将参数取出赋给command对象
        else {
            String[] params = content.split(command.delimiter);
            if(params.length == 1) command.parameters = null;
            else {
                String[] realParams = new String[params.length - 1];
                System.arraycopy(params, 1, realParams, 0, realParams.length);
                command.parameters = realParams;
            }
        }
    }

    public void test() throws IllegalHandlingMethodException {
        addCommandToRootNode(new Command("apple"));
        addCommandToRootNode(new Command("any"));
        addCommandToRootNode(new Command("approach"));
        addCommandToRootNode(new Command("apply"));
        addCommandToRootNode(new Command("ant"));
        addCommandToRootNode(new Command("aplet"));
        addCommandToRootNode(new Command("approve"));
        addCommandToRootNode(new Command("applicate"));
        addCommandToRootNode(new Command("anyone"));
        realTreeHeight = calculateMaxTreeHeight(rootNode) - 1;
    }
}
