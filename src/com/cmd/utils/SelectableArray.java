package com.cmd.utils;

import java.util.Iterator;

/**
 * 可选择的数组
 * 逻辑上由元素池和被选择的数组构成，但实际上维护了一个T类型的数组
 * 构造函数中，传入一个T类型的数组，这便是SelectableArray的初始元素池
 * SelectableArray的初始长度为0，通过select方法从元素池中选择元素，并提供get、set方法来访问选出来的元素
 * 对于元素池ElementPool对象，ElementPool类也提供了get、set方法以便访问元素池中的元素
 * 通过SelectableArray内的getElementPool方法即可获取当前维护的元素池对象
 * 若SelectableArray已经选择了一些元素，可以通过refactor方法将已经选择的元素作为一个新的元素池替换掉原本的元素池 
 * 同时此时SelectableArray的长度变为0，如果想选择元素池中所有的元素，可以调用selectAll方法
 * 无论经过多少次refactor，都可以通过reset方法回归最初的选择状态及元素池排列
 * 注意，尽量不要重复选择元素，当选择的元素总数大于元素池的元素个数时，会发生数组越界，如需重选，请另辟蹊径
 * 各方法使用详情见方法注释
 * @author congxiaoyao
 * @date 2015.12.22
 * @version 1.0
 * @param <T>
 */
public class SelectableArray<T> implements Iterable<T> {
	
	private T[] data;
	private ElementPool<T> pool;
	private int[] selected;				//用于记录被选择的元素在data中的位置
	private int pointer;				//下一个select后新元素要放入select数组中的位置

	public SelectableArray(T[] elementPool) {
		this.data = elementPool;
		pool = new ElementPool<T>(elementPool.length);
		selected = new int[elementPool.length];
		pointer = 0;
	}
	
	/**
	 * 从元素池中选择索引为index的元素
	 * @param index 若元素池重构过，以重构后的元素池所组成的元素排列为标准的index
	 */
    public void select(int index) {
        if (index >= pool.size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        selected[pointer++] = pool.elements[index];
    }
    
    /**
     * 选择全部，选择完后的顺序是被选择元素的原始顺序
     * 如果之前选择过一些再来调用此方法，之前选择的顺序将会被打乱
     */
    public void selectAll() {
        pointer = 0;
        for (int i = 0; i < pool.size; i++) {
            selected[pointer++] = pool.elements[i];
        }
    }
    
    /**
     * 从已选择的数组里获取下标为index的元素
     * @param index 以选择出来的元素组成的数组作为标准的index
     */
    public T get(int index) throws ArrayIndexOutOfBoundsException{
        if (index >= pointer) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return (T) data[selected[index]];
    }
    
    /**
     * 以选出的元素作为被操作的数组，给位置index赋值element,同时elementPool也会因此受到影响
     * 注意赋值操作是不可逆的，不能通过reset方法恢复初始状态
     * @param index 以选择出来的元素组成的数组作为标准的index
     * @param element T类型元素
     */
    public void set(int index, T element) throws ArrayIndexOutOfBoundsException {
        if (index >= pointer) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        data[selected[index]] = element;
    }
    
    /**
     * @return 选出来的数组的长度
     */
    public int size() {
        return pointer;
    }
    
    /**
     * 重构！！
     * 将已被选择的数组作为新的元素池替换掉之前的，同时清空所有已经选择的元素
     */
    public void refactor() {
        System.arraycopy(selected, 0, pool.elements, 0, pointer);
        pool.size = pointer;
        pointer = 0;
    }
    
    /**
     * 重置函数，撤销所有的重构操作，回到构造函数完成时候的状态
     * 注意，set函数的操作是不可逆的，通过set函数修改的值不可能被reset回来
     */
    public void reset() {
    	pool.reset();
        pointer = 0;
    }
    
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<T> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < pointer;
        }

        @Override
        public T next() {
            return get(index++);
        }
    }
	
    /**
     * @return 正在维护的元素池对象
     */
	public ElementPool<T> getElementPool() {
		return pool;
	}
	
	/**
	 * 无论是通过select形成被选择的数组还是重构元素池，都是通过指针数组来进行操作，除了set方法
	 * data字段中的元素不可能被任何一个方法所修改
	 * ElementPool维护了一个指针数组，用于记录当前元素池中每一个位置所对应的data中的元素
	 * @author core
	 * 
	 * @param <E> E类型要与SelectableArray的T类型相同，否则会发生类型转换错误
	 */
	public class ElementPool<E> implements Iterable<E>{
		private int[] elements;
		private int size;
		
		private ElementPool(int size) {
			this.size = size;
			this.elements = new int[size];
			for(int i=0;i<size;i++) elements[i] = i;
		}
		
		/**
		 * 对于元素池中的某个元素的赋值，或会影响到已经选择的数组中的某个元素的值
		 * @param index 以元素池中元素所组成的元素排列为标准的index
		 * @param element 必须要为T类型
		 * @throws ArrayIndexOutOfBoundsException
		 */
		@SuppressWarnings("unchecked")
		public void set(int index , E element) throws ArrayIndexOutOfBoundsException{
			if(index >= size) throw new ArrayIndexOutOfBoundsException(index);
			data[elements[index]] = (T) element;
		}
		
		@SuppressWarnings("unchecked")
		public E get(int index) throws ArrayIndexOutOfBoundsException{
			if(index >= size) throw new ArrayIndexOutOfBoundsException(index);
			return (E) data[elements[index]];
		}
		
		public int size() {
			return size;
		}
		
		protected void reset(){
			size = elements.length;
			for(int i=0;i<size;i++) elements[i] = i;
		}

		@Override
		public Iterator<E> iterator() {
			return new ElementPoolItr();
		}
		
		class ElementPoolItr implements Iterator<E>{
			int index = 0;
			
			@Override
			public boolean hasNext() {
				return index < size;
			}

			@SuppressWarnings("unchecked")
			@Override
			public E next() {
				return (E) data[elements[index++]];
			}
			
		}
	}
}
