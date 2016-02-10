package com.congxiaoyao.cmd;

/**
 * 快速排序算法的封装类(适配器?) 本类只针对一种情况进行排序，
 * 当我们自己实现了一种元素容器（像是ArrayList、Vector)但并没有实现标准的List接口时
 * 系统无法为我们提供排序算法,此类的作用就是适配这种容器并提供快速排序算法
 * 其中泛型{@code Container}为容器的类型，泛型{@code Element}为容器内包裹的元素的类型
 * 请先实现三个abstract方法以便在排序时可以交换元素位置及确定容器大小
 * 如有特殊需要或更高效的元素交换方法可以覆写{@code QuickSort#swap(int, int, Object)}函数
 *
 * Created by congxiaoyao on 2016/2/8.
 * @version 1.0
 */
public abstract class QuickSort<Container , Element> {

    /**
     * 排序入口函数，通过快排实现排序
     * @param container 容器，可以理解为带排序数组
     */
    public void sort(Container container) {
        if (container != null) {
            quickSort(0, size(container) - 1, container);
        }
    }

    /**
     * 递归版快排，对container中的元素进行排序
     * @param start 开始排序的范围
     * @param end   结束排序的范围
     * @param container 容器，可以理解为待排序数组
     */
    private void quickSort(int start, int end, Container container) {
        if (start >= end) return;

        int startOrg = start;
        int endOrg = end;
        Element refer = get(container, start);

        while (start < end) {

            while (compare(get(container, end), refer) >= 0 && start < end) {
                end--;
            }
            while (compare(get(container, start), refer) <= 0 && start < end) {
                start++;
            }

            swap(start, end, container);
        }
        swap(startOrg, end, container);

        quickSort(startOrg, end - 1, container);
        quickSort(end + 1, endOrg, container);
    }

    /**
     * 交换container中的位于index0及index1的两个元素
     * @param index0
     * @param index1
     * @param container
     */
    public void swap(int index0, int index1, Container container){
        Element temp = get(container, index0);
        set(container, get(container, index1), index0);
        set(container, temp, index1);
    }

    /**
     * 如果希望QuickSort从小到大排序，请用 element0-element1，否则用 element1-element0
     * @param element0
     * @param element1
     * @return element0 大于 element1 要返回正数 element0 小于 element1 要返回负数 相等返回0
     */
    public abstract int compare(Element element0, Element element1);

    /**
     * @param container
     * @param index
     * @return container中位于index的元素
     */
    public abstract Element get(Container container, int index);

    /**
     * 给container的index位置赋值
     * @param container
     * @param element
     * @param index
     */
    public abstract void set(Container container, Element element, int index);

    /**
     * @param container
     * @return container的大小，也可以理解为想要排序的部分的长度
     */
    public abstract int size(Container container);
}