package com.congxiaoyao.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * class文件的热加载类，使得cmd框架动态添加和处理命令成为可能
 * 单纯的通过命令添加命令是无意义的，添加命令的意义在于能够通过添加的这条命令处理新的事务
 * 基于{@code DynamicClassLoader}我们提供了handle_with命令，他的处理函数实现在CommandWindowHandler中
 * handle_with命令需要给定一个参数 与{@code CommandAnalyzerManager#handleWith(Object) }不同的是
 * 这里的参数是handlingObject的类名而不是类的实例
 * 举个例子，如果我们动态添加了一条命令sayhello，我们又新建了类com.cmd.Handler，将他的处理函数写入了这个类
 * 并标明了注释<code>@CommandName("sayhello")</code>
 * 这时只要将这个类编译一下，即可通过命令 handle_with com.cmd.Handler 实现与
 * 代码<code>CommandAnalyzerManager.handleWith(new Handler());</code>相同的功能
 * 也就是给命令sayhello对应的处理函数 从而完成动态命令处理
 *
 * @author congxiaoyao
 * @version 1.0
 * @date 2016.2.12
 */
public class DynamicClassLoader extends ClassLoader {

    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    @SuppressWarnings("unchecked")
    public Class loadClass(String classPath, String className)
            throws ClassNotFoundException {
        try {
            className += ".class";
            String url = classPathParser(classPath) + classNameParser(className);
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            InputStream input = connection.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int data = input.read();
            while (data != -1) {
                buffer.write(data);
                data = input.read();
            }
            input.close();
            byte[] classData = buffer.toByteArray();
            return defineClass(noSuffix(className), classData, 0,
                    classData.length);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String pathParser(String path) {
        return path.replaceAll("\\\\", "/");
    }

    private String classPathParser(String path) {
        String classPath = pathParser(path);
        if (!classPath.startsWith("file:")) {
            classPath = "file:" + classPath;
        }
        if (!classPath.endsWith("/")) {
            classPath = classPath + "/";
        }
        return classPath;
    }

    private String classNameParser(String className) {
        return className.substring(0, className.lastIndexOf(".")).replaceAll(
                "\\.", "/")
                + className.substring(className.lastIndexOf("."));
    }

    private String noSuffix(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }
}