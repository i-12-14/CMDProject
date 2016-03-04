package com.cmd.core;

import com.cmd.annotations.Outline;
import com.cmd.utils.CmdUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * 比CommandAnalyzer性能更好的命令解析器，针对命令匹配、字串分割做了额外的优化
 * 不同于CommandAnalyzer，FastAnalyzer建立了多级索引(一棵搜索树)，对于每一层的索引，
 * 使用二分搜索做查找，使得在命令定义比较密集的情况下查找效率比CommandAnalyzer高三到四倍
 * 同时对内存的消耗也比使用hashMap低得多，兼顾性能与内存消耗<p>
 * FastAnalyzer针对一种特定情况对字符串分割做出了优化，当命令使用一个字符作为分隔符时
 * FastAnalyzer对字串的分割效率理想状态下能达到jdk给出的分割算法的三到四倍(大部分命令的分隔符都是单字符的)
 * 所以，两部分结合起来，在分析阶段性能的提升是显著的，但无可避免的在其他方面做出了妥协<p>
 * 由于使用树来管理command对象，所以无法实现Analysable接口的getCommands方法，这是框架设计上的一个失误
 * 由于使用树来管理command对象，导致其在内存上的消耗一定是比CommandAnalyzer要高很多的<p>
 *
 * 在框架设计之初，没有考虑到会走到今天这一步，所以使得FastAnalyzer与Analysable接口有些许的不兼容
 * 可能会导致某些额外功能的不稳定 如代码提示器或各种handler，今后的版本将着力修复
 *
 * @version 2.4
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

    /**
     * 构造函数，给父类的构造函数传参false，即不使用父类的命令集合来维护command对象
     * FastAnalyzer使用树的结构来维护所有的command从而提高查找效率
     */
    private FastAnalyzer() {
        super(false);
        rootNode = new Node('\0');
        outlineMap = new TreeMap<>();
        initTypesMap();
    }

    /**
     * 覆写父类维护command对象的方式，将解析出来的 command对象放入搜索树中
     * @param handlingObject 包含处理函数的对象
     * @return
     */
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
                int cmdNameLen = temp.commandName.length();
                if (cmdNameLen > realTreeHeight) {
                    realTreeHeight = cmdNameLen;
                }
            } catch (IllegalHandlingMethodException e) {
                e.printStackTrace();
            }
        }
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
            if(existed.isDelimiterEquals(command.delimiter)){
                existed.addHandlingMethod(command.getHandlingMethods().get(0));
                return;
            }
        }
        //如果找不到就直接添加进去
        node.addCommand(command);
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

    /**
     * 通过FastAnalyzer维护的搜索树来查找相应的命令并解析
     * @param content 用户提交的字符串
     * @return
     */
    @Override
    public Command analyze(String content) {
        //计算实际需要分析的字符串的长度
        int len = content.length() > realTreeHeight ? realTreeHeight : content.length();
        char[] inputs = content.toCharArray();
        int nowIndex = -1;
        Node finder = new Node(rootNode.c);
        Node nowNode = rootNode;
        for (int i = 0; i < len; i++) {
            finder.c = inputs[i];
            //如果没有下一层，说明没有匹配到命令
            if(nowNode.nextLayer == null) return null;
            nowNode = nowNode.findNodeInNextLayer(finder);
            //如果下一层中没想要的东西 说明没匹配到
            if(nowNode == null) return null;
            nowIndex++;
            Command[] commands = nowNode.commands;
            if (nowNode.commands == null) continue;
            //在当前的node中保存有command对象，尝试匹配下看看是不是用户输入的这条
            for (Command command : commands) {
                //如果是用户输入的，可以继续去分析具体输入的参数了
                if (isDelimiterMatch(command.delimiter, content, nowIndex+1)) {
                    analyzeCommandParam(command, inputs, content);
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
        if(delimiter == null) return true;
        int delimiterLen = delimiter.length();
        if (delimiterLen + startIndex > content.length()) return false;
        return delimiter.equals(content.substring(startIndex, startIndex + delimiterLen));
    }

    /**
     * 已经确定用户输入content是命令command，现在要根据command中的分隔符信息从content中解析出参数来
     * @param command
     * @param content
     */
    private void analyzeCommandParam(Command command, char[] content, String sContent) {
        int cmdNameLen = command.commandName.length();
        int contentLen = content.length;
        //输入了一个无参命令
        if (cmdNameLen == contentLen) command.parameters = null;
        //无分隔符情况
        else if (command.delimiter == null) {
            String param = new String(content, cmdNameLen, contentLen - cmdNameLen);
            command.parameters = new String[]{param};
        }
        //通过分隔符将参数取出赋给command对象
        //输入可能为 commandName_XXX_XXX_XXX或commandName_
        //分隔符为一个字符的按优化算法分割
        else if (command.delimiter.length() == 1)
            analyzeCommandParamBranch(command, content, cmdNameLen);
        //分隔符长度大于1的按jdk提供的分割算法分割
        else analyzeCommandParamBranch(command, sContent);
    }

    /**
     * 解析命令参数的一个逻辑上的分支，专门处理 输入可能为 commandName_XXX_XXX_XXX或commandName_的情况
     * @param command 要求分隔符长度最好为2或以上，因为对长度为1的分隔符提供了优化的分割算法
     * @param content
     */
    public void analyzeCommandParamBranch(Command command, String content) {
        String[] params = content.split(command.delimiter);
        if (params.length == 1) command.parameters = null;
        else {
            String[] realParams = new String[params.length - 1];
            System.arraycopy(params, 1, realParams, 0, realParams.length);
            command.parameters = realParams;
        }
    }

    /**
     * 解析命令参数的一个逻辑上的分支，专门处理 输入可能为 commandName_XXX_XXX_XXX或commandName_的情况
     * @param command 要求分隔符长度必须为1，这里针对jdk中的分割算法做了优化
     * @param content
     * @param cmdNameLen
     */
    public void analyzeCommandParamBranch(Command command, char[] content, int cmdNameLen) {
        String[] params = CmdUtils.split(content, cmdNameLen + 1, command.delimiter.charAt(0));
        if (params.length == 0) {
            command.parameters = null;
        }
        else command.parameters = params;
    }

    /**
     * 禁止父类函数对长度为1的分割符做转义操作
     * @param delimiter
     * @return 见父类注解
     */
    @Override
    protected String reanalyseDelimiter(String delimiter, Method method) {
        if (delimiter.length() == 1) {
            return delimiter;
        }
        return super.reanalyseDelimiter(delimiter,method);
    }

    @Override
    public void forEachCommand(Consumer<Command> consumer) {
        rootNode.iterateChild(consumer);
    }

    @Override
    public void removeCommand(Command command) {
        char[] chars = command.commandName.toCharArray();
        Node finder = new Node(rootNode.c);
        Node nowNode = rootNode;
        for (char c : chars) {
            finder.c = c;
            nowNode = nowNode.findNodeInNextLayer(finder);
            if (nowNode == null) {
                return;
            }
        }
        Command[] commands = nowNode.commands;
        if (commands == null) return;
        for (int i = 0; i < commands.length; i++) {
            Command existed = commands[i];
            if (existed.isDelimiterEquals(command.delimiter)) {
                nowNode.removeCommand(i);
                return;
            }
        }
    }

    @Deprecated
    @Override
    public List<Command> getCommands() {
        return null;
    }
}