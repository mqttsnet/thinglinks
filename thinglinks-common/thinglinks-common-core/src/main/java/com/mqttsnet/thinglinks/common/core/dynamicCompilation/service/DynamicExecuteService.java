package com.mqttsnet.thinglinks.common.core.dynamicCompilation.service;


import com.mqttsnet.thinglinks.common.core.dynamicCompilation.ClassInjector;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.DynamicClassLoader;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.DynamicLoaderEngine;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode.InjectionSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态执行服务
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class DynamicExecuteService {
    private URLClassLoader pClassLoader;
    
    /**
     * 构造函数
     * 
     * @param pClassLoader 父类加载器
     */
    public DynamicExecuteService(URLClassLoader pClassLoader) {
        this.pClassLoader = pClassLoader;
    }
    
    /**
     * 动态执行代码</BR>
     * 1、使用自定义类加载器加载类</BR>
     * 2、修改类中的java.lang.System类为InjectionSystem</BR>
     * 3、将InjectionSystem的out输出到ByteArrayOutputStream</BR>
     * 4、执行过程中的所有错误都整理成字符串返回</BR>
     * 5、类中的执行结果也通过字符串返回</BR>
     * 6、4和5的输出结果不会同时存在</BR>
     * 
     * @param code 源码
     * @return 返回错误或者类的执行输出
     */
    public String executeDynamically(String code) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(buffer, true);

        try {
            String path = "";
            path = getClassPath(path);

            List<String> options = new ArrayList<String>();
            options.add("-classpath");
            options.add(path);
            options.add("-encoding");
            options.add("UTF-8");

            byte[] classBytes = DynamicLoaderEngine.compile(code, out, options);
            byte[] injectedClass = ClassInjector.injectSystem(classBytes);

            InjectionSystem.inject(null, new PrintStream(buffer, true), null);

            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(pClassLoader);
            DynamicLoaderEngine.executeMain(dynamicClassLoader, injectedClass, out);
            
            InjectionSystem.restore();
        }
        catch (Throwable t) {
            t.printStackTrace(out);
        }
        finally {
            out.close();
        }

        return buffer.toString();
    }

    private String getClassPath(String path) {
        
        URLClassLoader classLoader = pClassLoader;
        while (true) {
            for (URL url : classLoader.getURLs()) {
                path = path + url.getFile() + File.pathSeparator;
            }
            
            if (null == classLoader.getParent()) {
                break;
            }
            
            if (classLoader.getParent() instanceof URLClassLoader) {
                classLoader = (URLClassLoader) classLoader.getParent();
            }
            else {
                break;
            }
        }
        
        return path;
    }
}