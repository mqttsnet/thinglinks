package com.mqttsnet.thinglinks.common.core.dynamicCompilation;


import com.mqttsnet.thinglinks.common.core.dynamicCompilation.compiler.ClassCompiler;

import java.io.PrintWriter;
import java.util.List;

/**
 * 动态加载引擎，负责动态编译、加载类
 *
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class DynamicLoaderEngine {
    
    public static byte[] compile(String javaCode, PrintWriter out, List<String> options) {
        try {
            ClassCompiler classCompiler = new ClassCompiler();
            byte[] classBytes = classCompiler.compile(javaCode, out, options);
            if (null == classBytes) {
                return null;
            }
            
            return classBytes;
        }
        catch (Throwable t) {
            t.printStackTrace(out);
        }
        
        return null;
    }
    
    public static Class<?> loadClass(DynamicClassLoader classLoader, byte[] classBytes, PrintWriter out){
        try {
            Class<?> dynamicClass = classLoader.loadClassByBytes(classBytes);
            if (null == dynamicClass) {
                out.println("Failed to load class.");
                return null;
            }
            
            return dynamicClass;
        }
        catch (Throwable t) {
            t.printStackTrace(out);
        }
        
        return null;
    }
    
    /**
     * 加载类
     * 加载失败，则返回null, 同时out中包含错误信息
     * 
     * @param classLoader 类加载器
     * @param javaCode 源码
     * @param out 错误信息输出
     * @param options 编译过程中的参数
     * @return
     */
    public static Class<?> loadClass(DynamicClassLoader classLoader, String javaCode, PrintWriter out, List<String> options) {
        try {
            byte[] classBytes = compile(javaCode, out, options);
            
            if (null == classBytes) {
                return null;
            }
            
            return loadClass(classLoader, classBytes, out);
        }
        catch (ClassFormatError e) {
            e.printStackTrace(out);
        }

        return null;
    }
    
    /**
     * 执行主类
     */
    public static boolean executeMain(DynamicClassLoader classLoader, String javaCode, PrintWriter out, List<String> options) {
        Class<?> loadedClass = loadClass(classLoader, javaCode, out, options);
        if (null == loadedClass) {
            return false;
        }
        
        return executeMain(loadedClass, out);
    }
    
    public static boolean executeMain(DynamicClassLoader classLoader, byte[] classBytes, PrintWriter out) {
        Class<?> loadClass = loadClass(classLoader, classBytes, out);
        if (loadClass == null) {
            return false;
        }
        
        return executeMain(loadClass, out);
    }
    
    public static boolean executeMain(Class<?> loadedClass, PrintWriter out) {
        ClassExecutor classExecutor =  new ClassExecutor();
        return classExecutor.executeMain(loadedClass, out);
    }
}