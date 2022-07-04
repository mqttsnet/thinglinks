package com.mqttsnet.thinglinks.common.core.dynamicCompilation;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 自定义类加载器，用于动态加载类 
 * </BR>继承自 @see URLClassLoader
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class DynamicClassLoader extends URLClassLoader {
    public DynamicClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class<?> findClassByClassName(String className) throws ClassNotFoundException {
        return this.findClass(className);
    }

    public Class<?> loadClassByBytes(byte[] classData) {
        return this.defineClass(null, classData, 0, classData.length);
    }
}