package com.mqttsnet.thinglinks.common.core.jdk;

import com.momo.xcode.common.XCodeConstant;
import com.momo.xcode.compile.jdk.javafileobject.CharSequenceJavaFileObject;
import com.momo.xcode.util.CodeUtil;
import com.momo.xcode.util.MD5Util;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDK动态编译器
 *
 * <p>主要功能：
 * <p>通过JDK自带的编译器将一段代码动态编译成Class，并加载至JVM中
 *
 * @author moqinggen
 * @date 2021/07/01
 */
public class JDKDynamicCompiler {

    /**
     * 编译class缓存
     */
    private static Map<String, Class<?>> classCacheMap = new ConcurrentHashMap<>();

    /**
     * 编译code
     *
     * @param code 需要编译的代码
     * @return
     */
    public static Class<?> compile(String code) throws Throwable {
        // 提取代码中类名
        String[] classNameInfos = CodeUtil.getClassNameInfos(code);
        if (classNameInfos == null) {
            throw new RuntimeException("code错误，无法获取到类名");
        }
        // 修复注释引起的编译错误
        code = CodeUtil.fixCode(code);

        // 编译后类根路径
        String rootClassPath = JDKDynamicCompiler.class.getClassLoader().getResource("").getPath();

        // 生成、加载class
        Class<?> newClass = generate(code, classNameInfos, rootClassPath);
        if (null == newClass) {
            throw new RuntimeException("生成类失败");
        }
        return newClass;
    }


    /**
     * 生成、加载class
     *
     * @pa
     */
    private static Class<?> generate(String code, String[] classNameInfos, String rootClassPath) throws Throwable {
        // 0.从本地缓存获取
        String codeMd5 = MD5Util.encode2String(code);
        if (classCacheMap.containsKey(codeMd5)) {
            return classCacheMap.get(codeMd5);
        }

        // 1. 生成新的类名，格式：className_timestamp
        String newClassName = classNameInfos[1] + XCodeConstant.UNDER_LINE + System.currentTimeMillis();
        String newFullClassName = classNameInfos[0] + XCodeConstant.DOT + newClassName;
        // 替换code中原来的类名
        code = code.replace(classNameInfos[1], newClassName);


        // 2. JDK动态编译
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // 2.1 编译参数，可以参考 javac -d xxx Test.java
        List<String> options = Arrays.asList("-d", rootClassPath);

        // 2.2 java文件对象
        JavaFileObject fileObject = new CharSequenceJavaFileObject(newFullClassName, code);
        List<JavaFileObject> compilationUnits = Arrays.asList(fileObject);

        // 2.3 提交编译任务
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, compilationUnits);

        // 2.4 动态编译
        boolean success = task.call();
        if (!success) {
            return null;
        }

        // 3. 加载类
        URLClassLoader classLoader = null;
        try {
            // 3.1 URLClassLoader
            // 根目录，格式：file:/xxx
            URL url = new URL("file:" + rootClassPath);
            URL[] urls = {url};
            classLoader = new URLClassLoader(urls);

            // 3.2 加载class
            Class<?> clazz = classLoader.loadClass(newFullClassName);

            // 3.3 缓存
            classCacheMap.put(codeMd5, clazz);

            return clazz;
        } catch (Exception e) {

        } finally {
            if (null != classLoader) {
                classLoader.close();
            }
        }
        return null;
    }
}
