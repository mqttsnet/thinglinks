package com.mqttsnet.thinglinks.common.core.dynamicCompilation;


import com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode.ClassModifier;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode.InjectionSystem;

/**
 * 对类进行注入操作，即修改Class二进制中的字符串常量的符号引用，从而达到替换功能的目的
 *
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class ClassInjector {
    
    /**
     * 注入可注入式的IntectionSystem类来替换java.lang.System
     * </br>返回null，表示修改失败或者没有找到待修改的内容
     *
     * @param classBytes
     * @return 修改之后的class文件内容byte[]
     */
    public static byte[] injectSystem(byte[] classBytes) {
        return inject(classBytes, System.class, InjectionSystem.class);
    }
    
    /**
     * 注入Class类，修改其相应类的符号引用
     * </br>返回null，表示修改失败或者没有找到待修改的内容
     *
     *
     * @param classBytes 待修改的class内容
     * @param oldClassStrRef 老的类的符号引用
     * @param newClassStrRef 新的类的符号引用
     * @return 修改之后的class文件内容byte[]
     */
    public static byte[] inject(byte[] classBytes, String oldClassStrRef, String newClassStrRef) {
        ClassModifier classModifier = new ClassModifier(classBytes);
        return classModifier.modifyUTF8Constant4Reference(oldClassStrRef, newClassStrRef);
    }
    
    public static byte[] inject(byte[] classBytes, Class<?> oldClass, Class<?> newClass) {
        ClassModifier classModifier = new ClassModifier(classBytes);
        return classModifier.modifyUTF8Constant4Class(oldClass, newClass);
    }
}
