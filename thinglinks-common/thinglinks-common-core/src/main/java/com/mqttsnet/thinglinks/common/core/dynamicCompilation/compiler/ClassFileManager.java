package com.mqttsnet.thinglinks.common.core.dynamicCompilation.compiler;


import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;

/**
 * 类文件管理器 * 用于JavaCompiler将编译好后的class,保存到jclassObject中
 *
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    /**
     * 保存编译后Class文件的对象 
     */
    private ClassJavaFileObject jclassObject;

    /**
     * 调用父类构造器 
     */
    public ClassFileManager(StandardJavaFileManager standardManager) {
        super(standardManager);
    }

    /**
     * JavaCompiler在编译java文件的时候，生成的class二进制内容会放到这个JavaFileObject中
     */
    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (jclassObject == null) {
            jclassObject = new ClassJavaFileObject(className, kind);
        }
        
        return jclassObject;
    }

    public ClassJavaFileObject getClassJavaFileObject() {
        return jclassObject;
    }
}