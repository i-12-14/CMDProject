package com.congxiaoyao.cmd;

import java.util.Iterator;

/**
 * 这是一个有大小限制的容器，元素可以像ArrayList一样被添加到RoundList中
 * 当添加的元素个数大于限制个数时，第一个被添加的元素将会被从容器中移除
 * 而第二个元素将成为第一个元素，新添加的元素将成为最后一个
 * 以此类推，像是前面的被新添加的挤出去一样，有点类似于圆形队列
 * 添加元素过程并不产生元素位移、数组拷贝等操作，力求代码高效
 * 简单的工具类功能不多，只实现最有用的方法，不提供移除单个元素的操作，因为添加本身就有删除性质
 * @author congxiaoyao
 *
 * @param <T>
 * @date 2015.8.1		v1.0
 * @date 2015.9.12		v1.1	支持了简单的迭代器遍历
 * @date 2016.2.6		v1.2	添加了新功能，获取元素时索引可以为负数
 */
public class RoundList<T> implements Iterable<T> {
	
	private int tail = 0;		//roundlist的尾指针，待插入元素的索引
	private int size = 0;		//当前roundlist中实际存放的对象的个数
	
	private Object[] objects;	//用于存放泛型的object数组
	
	/**
	 * 通过一个最大的限制大小来构造一个roundlist
	 * 当roundlist中存储的元素数量超过limitSize时，最早被存储的元素将被挤出roundlist
	 * @param limitSize 最大容纳的元素的个数，也就是限制个数
	 */
	public RoundList(int limitSize) {
		objects = new Object[limitSize];
	}
	
	/**
	 * 添加一个元素，如果roundlist中的元素个数超过限制的数量，则将最早被添加的元素挤出
	 * @param t 要被添加的元素
	 */
	public void add(T t) {
		//在尾指针处添加一个元素
		objects[tail] = t;
		//计算添加完这个元素实际存放的对象的个数
		if(size < objects.length) size++;
		//移动尾指针
		tail = (tail+1) % objects.length;
	}

	/**
	 * 当添加元素的次数超过最大上限时，为了获取最后添加的一个元素，参数应该传递limitSize-1
	 * 而被挤出的元素则不复存在
	 * @param index 元素在roundlist中的索引
	 * @return 获取逻辑上roundlist对应索引号所存放的元素
	 */
	@SuppressWarnings("unchecked")
	public T get(int index) {
		return (T) objects[(tail + index) % size];
	}
	
	/**
	 * {@code RoundList#get(int)}中的参数不允许是负数，否则可能会抛出数组越界异常
	 * 而这里允许使用负数作为index，得到的结果与参数的绝对值得到的结果相同
	 * 如果index等于limitSize时，获得的结果与index为0时的结果相等
	 * 如果为limitSize+1时，获得的结果与index为1时的结果相等，以此类推
	 * 当index小于0时，结果的分布规律与正数部分呈对称分布（关于0对称）
	 * @param index
	 * @return 获取逻辑上roundlist对应索引号所存放的元素
	 */
	public T getAllowsNegativeIndex(int index) {
		return get(index < 0 ? -index : index);
	}
	
	/**
	 * @return 得到roundList中的最后一个元素
	 */
	public T getLast(){
		if(size == 0)
			return null;
		return get(size-1);
	}
	
	/**
	 * @return 得到roundList中的第一个元素
	 */
	public T getFirst(){
		if(size == 0)
			return null;
		return get(0);
	}
	
	/**
	 * 移除roundlist中所有的元素
	 */
	public void removeAll(){
		//移除全部其实并不需要将所有的元素置为null，只需要改变尾指针位置及大小就可以
		tail = 0;
		size = 0;
	}
	
	/**
	 * 该函数的返回值范围为[0-limitSize]
	 * 当添加的元素数超过limitSize时，此方法总是返回limitSize
	 * @return roundlist中真实存储的元素的个数 
	 */
	public int size(){
		return size;
	}
	
	/**
	 * 返回一个roundlist的迭代器，这样roundlist也支持foreach遍历啦~
	 */
    public Iterator<T> iterator() {
        return new Itr();
    }
	
    /**
     *iterator的实现类
     */
	private class Itr implements Iterator<T>
	{
		private int index = -1;
		
		@Override
		public boolean hasNext() {
			return index < size -1;
		}

		@Override
		public T next() {
			return get(++index);
		}
	}
}