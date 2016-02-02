package com.congxiaoyao.cmd;

import java.util.Arrays;
import java.util.List;

/**
 * 代码提示器
 * 可有可无的一个类 但作为这套框架的一部分，能为使用者额外提供代码提示的功能
 * 我们假设本类是配合CommandAnalyzer一同使用，那只需要将CommandAnalyzer内维护的Command的List传入构造函数即可构造对象
 * 之后调用{@code CodeAssistor#find(String)}方法传入用户的当前输入即可得到代码提示的结果
 * 结果是以{@code SelectableArray<WeightedString>}的形式返回，SelectableArray支持foreach遍历
 * WeightedString中所有字段都是public访问权限的，其string字段代表了所谓的CommandName
 * Created by congxiaoyao on 2015/12/20.
 * @version 1.0
 */
public class CodeAssistor {

    private String lastContent = null;
    private WeightedString[] codes;
    private SelectableArray<WeightedString> seletableArray;

    public CodeAssistor(String[] codes) {
        int len = codes.length;
        this.codes = new WeightedString[len];
        for (int i = 0; i < len; i++) {
            this.codes[i] = new WeightedString(codes[i], 0);
        }
        seletableArray = new SelectableArray<>(this.codes);
    }
    
    public CodeAssistor(List<Command> commands) {
    	this(commandsToCodes(commands));
    }
    

    /**
     * 将用户输入传入即可得到符合查找标准的匹配度从高到低的查找结果
     * @param content 
     * @return 详见SelectableArray类头注释及WeightedString类头注释
     */
    public SelectableArray<WeightedString> find(String content) {
        //没有实际内容
        if(content.length() == 0) return seletableArray;
        //只是在上一次的基础上多加入了一些内容，上一次的搜索结果对于这一次搜索有帮助
        if (lastContent != null && content.length() > lastContent.length()
                && content.indexOf(lastContent) == 0) {
            seletableArray.refactor();
        }else {
            seletableArray.reset();
        }
        lastContent = content;
        //搜索符合条件的code
        char[] chars = content.toCharArray();
        SelectableArray<WeightedString>.ElementPool<WeightedString> pool = seletableArray.getElementPool();
        for (int i = 0, len = pool.size(); i < len; i++) {
            WeightedString weightedString = pool.get(i);
            if (isMatch(weightedString.string, chars)) {
                seletableArray.select(i);
            }
        }
        //按照相关度排序
        char[] contentChars = content.toCharArray();
        for (WeightedString  ws: seletableArray) {
            ws.weight = 0;
            for (int i = 0; i < contentChars.length; i++) {
                ws.weight += getDistance(ws.chars, contentChars[i], i);
            }
        }
        sort(seletableArray);
        return seletableArray;
    }

    /**
     * 获取char到指定位置的最短距离算法
     *
     * @param src    被搜索的字符串
     * @param target 目标字符
     * @param index  指定位置
     * @return src中所有target字符到index位置最近的距离
     */
    public static int getDistance(char[] src, char target, int index) {
        int distance = 0;
        int min = Integer.MAX_VALUE;
        for (int i = 0, len = src.length; i < len; i++) {
            if (target == src[i]) {
                distance = Math.abs(i - index);
                if (distance < min) min = distance;
            }
        }
        return min;
    }

    /**
     * 判断target中所有的字符是否能在src中找到，并且他们在src中出现的顺序符合在target中的顺序
     * @param src
     * @param target
     * @return 符合条件返回true
     */
    public static boolean isMatch(String src, char[] target) {
        int len = target.length, fromIndex = -1;
        for (int i = 0; i < len; i++) {
            if ((fromIndex = src.indexOf(target[i], fromIndex+1)) == -1) return false;
        }
        return true;
    }

    /**
     * 希尔排序，按WeightedString的weight字段从小到大排列
     * @param array SelectableArray
     */
    public static void sort(SelectableArray<WeightedString> array){
        int d=array.size();
        while((d/=2)>0){
            for(int i=d;i<array.size();i++){
                WeightedString weightedString = array.get(i);
                int temp=weightedString.weight,position=i;
                for(int j=i-d;j>=0;j-=d){
                	if(array.get(j).weight>=temp) break;
                	array.set(j+d,array.get(j));
                	position-=d;
                }
                array.set(position, weightedString);
            }
        }
    }
    
    /**
     * 将commands的集合中的CommandName转换为String的数组，同时重复的CommandName（因重载产生）将被剔除
     * @param commands
     * @return 可用于CodeReminder构造的String数组
     */
    public static String[] commandsToCodes(List<Command> commands) {
    	String[] codes = new String[commands.size()];
		int newLength = 0;
		for(int i=0;i<codes.length;i++) {
			String code = commands.get(i).commandName;
			boolean add = true;
			for(int j=0;j<i;j++)
				if(code.equals(codes[j])) add = false;
			if(add) codes[newLength++] = commands.get(i).commandName;
		}
		return Arrays.copyOf(codes, newLength);
    }

    /**
     * 带权值的String，对于任意一个code，其与用户输入的匹配度将记录在这个类的对象的weight字段，code在string字段
     * @author congxiaoyao
     */
    public class WeightedString {
        public String string;
        public int weight;
        public char[] chars;

        public WeightedString(String string, int weight) {
            this.string = string;
            this.weight = weight;
            chars = string.toCharArray();
        }

        @Override
        public String toString() {
            return "WeightedString{" +
                    "string='" + string + '\'' +
                    ", weight=" + weight +
                    '}';
        }
    }
}