package com.cmd.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * DFA算法？实现命令的匹配与查找，此类为多级搜索树中的一个节点，
 * 节点内保存有下一层的节点数组，搜索使用二分查找，添加时插入添加自动保持数组有序
 *
 * @version 2.4
 * Created by congxiaoyao on 2016/2/25.
 */
public class Node implements Comparable<Node> {

    private static final int INIT_LAYER_LEN = 5;

    public char c;                  //当前节点所代表的char

    public Node[] nextLayer;        //保存了当前节点的下一层所有的节点
    public int realLayerLen = 0;    //nextLayer的长度

    public Command[] commands;      //当这个数组不为null时或许到达了查找重点

    public Node(char c) {
        this.c = c;
    }

    /**
     * @param c
     * @return 如果在nextLayer中找到代表参数的节点返回nextLayer中的那个节点
     *         否则将参数代表的节点添加到nextLayer并返回新添加的节点
     */
    public Node findOrAddNodeToNextLayer(char c) {
        if (nextLayer == null) {
            nextLayer = new Node[INIT_LAYER_LEN];
            Node node = new Node(c);
            nextLayer[0] = node;
            realLayerLen = 1;
            return node;
        }
        //遍历搜索nextLayer中存不存在参数给定的节点,存在就返回了
        Node node = new Node(c);
        int index = Arrays.binarySearch(nextLayer, 0, realLayerLen, node);
        if (index >= 0) return nextLayer[index];
        //如果数组满了 扩容之，以二倍速率扩容
        if (nextLayer.length == realLayerLen) {
            Node[] newNodes = new Node[nextLayer.length * 2];
            System.arraycopy(nextLayer, 0, newNodes, 0, nextLayer.length);
            nextLayer = newNodes;
        }
        //计算插入点
        index = -index - 1;
        //位移元素
        System.arraycopy(nextLayer, index, nextLayer, index + 1, realLayerLen - index);
        nextLayer[index] = node;
        realLayerLen++;
        return node;
    }

    /**
     * 添加一个command对象
     * @param command
     */
    public void addCommand(Command command) {
        //如果commands对象为空就new出来把command对象加进去
        if (commands == null) {
            commands = new Command[]{command};
            return;
        }
        //对于每一次新来的command对象，数组中是没有多余的空间的，要先扩容再添加
        int len = commands.length;
        Command[] newCommands = new Command[len + 1];
        System.arraycopy(commands, 0, newCommands, 0, len);
        newCommands[len] = command;
        commands = newCommands;
    }

    /**
     * @param node
     * @return 去nextLayer中寻找参数节点，没有返回null
     */
    public Node findNodeInNextLayer(Node node) {
        int index = Arrays.binarySearch(nextLayer, 0, realLayerLen, node);
        return index >= 0 ? nextLayer[index] : null;
    }

    /**
     * 深度优先搜索这个节点的所有子节点，去寻找藏匿在其中的所有的command对象并通知回调
     * @param consumer
     */
    public void iterateChild(Consumer<Command> consumer) {
        if (nextLayer == null) return;
        iterateNextLayer((node -> {
            Command[] targets = node.commands;
            if (targets != null) {
                for (Command target : targets) {
                    consumer.accept(target);
                }
            }
            node.iterateChild(consumer);
        }));
    }

    /**
     * 安全的迭代nextLayer中的所有节点
     * @param consumer
     */
    public void iterateNextLayer(Consumer<Node> consumer) {
        for (int i = 0; i < realLayerLen; i++) {
            consumer.accept(nextLayer[i]);
        }
    }

    @Override
    public int compareTo(Node o) {
        return c - o.c;
    }

    @Override
    public boolean equals(Object o) {
        return c == ((Node) o).c;
    }

    @Override
    public int hashCode() {
        return (int) c;
    }

    @Override
    public String toString() {
        return "" + c;
    }
}