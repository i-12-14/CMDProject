package com.cmd.core;

import com.cmd.annotations.CmdDef;
import com.cmd.annotations.OnlyCare;
import com.cmd.utils.CmdUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 与命令绑定的函数称作处理函数，此类定义了一个处理函数所拥有的各种属性
 * 可通过以下注解绑定命令与处理函数
 *
 * @see com.cmd.annotations.CmdDef
 * @see com.cmd.annotations.CommandName
 * @see com.cmd.annotations.Delimiter
 * @see com.cmd.annotations.Description
 *
 * @version 2.1
 * Created by congxiaoyao on 2016/2/19.
 */
public class HandlingMethod {

    Method method;                      //反射出来的Method对象
    private String description;         //处理函数的描述
    private String[] careAbout;         //处理函数所携带的OnlyCare信息
    private int paramCount;             //处理函数的参数个数，可能会与Method中获取的参数个数不同
    private int onlyCareCount = 0;      //函数上带有多少个OnlyCare注解(包括参数上)
    private Class<?>[] parameterTypes;  //处理函数的参数类型
    Object invoker;                     //用于反射调用
    VariableType variadicType;          //处理函数的参数的属性

    public HandlingMethod(Method method) throws BadDefinitionException {
        this.method = method;
        //分析VariadicType的值
        variadicType = VariableType.TYPE_IMMUTABLE;
        //检查处理函数参数类型是不是可变参数类型（Command类型或String数组类型）
        if (method.getParameterCount() == 1) {
            Class<?> type = getParameterTypes()[0];
            if (type == String[].class) {
                variadicType = VariableType.TYPE_STRING_ARRAY;
            } else if (type == Command.class) {
                variadicType = VariableType.TYPE_COMMAND;
            }
        }
        //分析paramCount个数及OnlyCare的内容
        paramCount = method.getParameterCount();
        //无参函数对于OnlyCare要额外处理，看其是否将OnlyCare标在了函数上而不是参数上
        if (paramCount == 0) {
            if (method.isAnnotationPresent(OnlyCare.class)) {
                OnlyCare onlyCare = method.getAnnotation(OnlyCare.class);
                careAbout = new String[]{onlyCare.value()};
                //无参函数加OnlyCare注解，我们认为这是一个处理一参命令的函数
                paramCount = 1;
                onlyCareCount = 1;
            }
            checkDefinitionException();
            return;
        }
        Parameter[] parameters = method.getParameters();
        //因为不一定所有的参数都有OnlyCare注解，甚至所有的参数都有OnlyCare注解
        //所以为了节约内存，careAbout数组的长度由最后一个标有OnlyCare注解的参数的位置决定
        int index = -1;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(OnlyCare.class)) {
                index = i;
            }
        }
        //如果没有参数标有OnlyCare注解，直接返回
        if (index == -1) {
            checkDefinitionException();
            return;
        }
        //将OnlyCare中的参数放到careAbout数组中
        careAbout = new String[index + 1];
        for (int i = 0; i < careAbout.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(OnlyCare.class)) {
                OnlyCare onlyCare = parameter.getAnnotation(OnlyCare.class);
                String careWhat = onlyCare.value();
                //如果OnlyCare注解中没有参数，将方法的参数名当做注解的参数
                if (careWhat.equals("")) {
                    careWhat = parameter.getName();
                }
                onlyCareCount++;
                careAbout[i] = careWhat;
            }
        }
        checkDefinitionException();
    }

    /**
     * 检查用户定义时出现的问题并抛出异常
     */
    private void checkDefinitionException() throws BadDefinitionException {
        //在处理函数中带参数的情况下将OnlyCare标记在函数上
        if (method.getParameterCount() >= 1 && method.isAnnotationPresent(OnlyCare.class)) {
            throw new BadDefinitionException(BadDefinitionException.ONLYCARE_ERROR,
                    method.toString());
        }
        //将OnlyCare标记在了可变参数(Command类型或String[]类型)上
        if (isOnlyCareAnnotated() && parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if(getOnlyCareByParam(i) != null &&
                        CmdUtils.isVarTypes(parameterTypes[i])){
                    throw new BadDefinitionException(BadDefinitionException.ONLYCARE_ERROR,
                            method.toString());
                }
            }
        }
        //将CmdDef注解与另外的三个注解混合使用
        if (method.isAnnotationPresent(CmdDef.class)) {
            Class<? extends Annotation>[] annotations = CmdUtils.CMD_ANNOTATIONS;
            for (Class<? extends Annotation> annotation : annotations) {
                if (method.isAnnotationPresent(annotation)) {
                    throw new BadDefinitionException(BadDefinitionException.DECLARE_ERROR,
                            method.toString());
                }
            }
        }
    }

    /**
     * @return 处理函数上(或其参数上)标有OnlyCare注解返回true
     */
    public boolean isOnlyCareAnnotated() {
        return careAbout != null;
    }

    /**
     * @param paramIndex 从0开始
     * @return 获取此方法参数上标记的OnlyCare注解的参数，没有返回null
     * 对于无参方法，方法上带有OnlyCare注解也可以用这种方式获取
     * 注意！！调用次方法前最好调用isOnlyCareAnnotated，除非你有十足的把我
     */
    public String getOnlyCareByParam(int paramIndex) {
        if (careAbout.length <= paramIndex) return null;
        return  careAbout[paramIndex];
    }

    /**
     * 判断这个处理函数是否合法
     * @return 如果处理函数中同时存在可变参数类型（String数组或Command类型）及
     * 固定参数类型（基本参数类型）则不合法
     * 如果存在基本参数类型或其包装类型或Command类型或String数组类型之外的参数则不合法
     */
    public boolean isLegal() {
        Class<?>[] types = getParameterTypes();
        if (types.length > 1) {
            for (Class<?> type : types) {
                if (CmdUtils.isVarTypes(types[0]) || !CmdUtils.isBaseTypes(type)) {
                    return false;
                }
            }
        } else if (types.length == 1) {
            return CmdUtils.isBaseTypes(types[0]) || CmdUtils.isVarTypes(types[0]);
        }
        return true;
    }

    public Class<?>[] getParameterTypes() {
        if (parameterTypes == null) {
            parameterTypes = method.getParameterTypes();
        }
        return parameterTypes;
    }

    /**
     * @return 方法上带有OnlyCare注解的数量(参数上的也算是)
     */
    public int getOnlyCareCount() {
        return onlyCareCount;
    }

    /**
     * @return 此方法中参数的个数
     * 对于无参方法，如果方法上带有OnlyCare注解，则返回1
     */
    public int getParamCount() {
        return paramCount;
    }

    public Method getMethod() {
        return method;
    }

    public Object getInvoker() {
        return invoker;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 如果处理函数为一参函数且参数类型为String数组或Command类型则认为是可变参数类型
     */
    enum VariableType {
        //String数组类型
        TYPE_STRING_ARRAY,
        //Command类型
        TYPE_COMMAND,
        //不是可变参数类型
        TYPE_IMMUTABLE
    }
}
