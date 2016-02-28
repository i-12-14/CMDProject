package com.cmd.handler;

import com.cmd.annotations.CmdDef;
import com.cmd.core.*;
import com.cmd.utils.DynamicClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 关于框架的动态特性实践类，允许通过命令来添加或删除一个handlingObject
 * 允许通过命令来删除一个命令或是删除一个处理函数
 * 允许通过命令来重新加载一个handlingObject，甚至是重新编译并加载
 * 具体的哪一个函数对应什么功能，可以参看注解里的description参数
 * 功能或许不是太强大易用，但这里为框架的动态特性提供了一个简单的示例，足以证明这里是很有想象空间的
 *
 * @version 2.2
 * Created by congxiaoyao on 2016/2/23.
 */
public class DynamicCommandHandler extends BaseHandler {

    //存放class文件的根文件夹
    static final String CLASS_PATH = new File("bin").getAbsolutePath();
    //存放源文件的根文件夹
    static final String SRC_PATH = new File("src").getAbsolutePath();

    public DynamicCommandHandler(Analysable analysable) {
        super(analysable);
    }

    @CmdDef(commandName = "addho",description = "添加一个处理类 请输入类的全名")
    public static void addHandlingObject(String className) {
        DynamicClassLoader classLoader = new DynamicClassLoader
                (DynamicClassLoader.class.getClassLoader());
        try {
            Object handlingObject = classLoader.loadClass(CLASS_PATH, className).newInstance();
            if (getAnalysable().getClass() == CommandAnalyzer.class) {
                CommandAnalyzer.handleWith(handlingObject);
            } else if (getAnalysable().getClass() == FastAnalyzer.class) {
                FastAnalyzer.handleWith(handlingObject);
            } else {
                System.out.println("处理失败");
                return;
            }
        } catch (InstantiationException |
                IllegalAccessException  | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("处理成功");
    }

    @CmdDef(commandName = "delcmd", description = "删除命令及其处理函数 请输入命令名")
    public static void removeCommandByName(String commandName) {
        List<Command> removeList = new ArrayList<>();
        getAnalysable().forEachCommand((command ->{
            if (command.commandName.equals(commandName)) {
                removeList.add(new Command(
                        new String(command.commandName),new String(command.delimiter)));
            }
        }));
        for (Command command : removeList) {
            getAnalysable().removeCommand(command);
        }
        System.out.println("处理完毕");
    }

    @CmdDef(commandName = "delhm", description = "删除一个处理函数 请输入完整函数签名",delimiter = "null")
    public static void removeHandlingMethod(String signature) {
        getAnalysable().forEachCommand(command -> {
            List<HandlingMethod> handlingMethods = command.getHandlingMethods();
            Iterator<HandlingMethod> iterator = handlingMethods.iterator();
            while (iterator.hasNext()) {
                HandlingMethod method = iterator.next();
                if (signature.equals(method.getMethod().toString())) {
                    iterator.remove();
                }
            }
            if (handlingMethods.isEmpty()) {
                System.out.print(NoneHandlingMethodException.MSG);
            }
        });
        System.out.println("处理完毕");
    }

    @CmdDef(commandName = "delho", description = "删除一个处理类 请输入类的全名")
    public static void removeHandlingObject(String className) {
        getAnalysable().forEachCommand(command -> {
            List<HandlingMethod> handlingMethods = command.getHandlingMethods();
            Iterator<HandlingMethod> iterator = handlingMethods.iterator();
            while (iterator.hasNext()) {
                HandlingMethod method = iterator.next();
                if (className.equals(method.getInvoker().getClass().getName())) {
                    iterator.remove();
                }
            }
            if (handlingMethods.isEmpty()) {
                System.out.print(NoneHandlingMethodException.MSG);
            }
        });
    }

    @CmdDef(commandName = "reload",description = "重新加载处理类 请输入类的全名")
    public static void reloadHandlingObject(String className) {
        removeHandlingObject(className);
        addHandlingObject(className);
    }

    @CmdDef(commandName = "refresh",description = "重编译加载处理类 请输入类的全名")
    public static void refreshHandlingObject(String className) {
        String fileName = SRC_PATH + "\\" + className.replaceAll("\\.", "\\\\") + ".java";
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        int result = javaCompiler.run(null, null, null,
                "-d", CLASS_PATH,
                "-encoding", "UTF-8", fileName);
        if (result != 0) {
            System.out.println("编译失败");
            return;
        }
        System.out.println("编译结束");
        reloadHandlingObject(className);
    }

    /**
     * @return 存放class文件的根文件夹,可根据实际需求覆写
     */
    public String getClassPath() {
        return CLASS_PATH;
    }

    /**
     * @return 存放源文件的根文件夹,可根据实际需求覆写
     */
    public String getSrcPath() {
        return SRC_PATH;
    }
}