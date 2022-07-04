package com.mqttsnet.thinglinks.common.core.dynamicCompilation.compiler;

import javax.tools.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 类编译器
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class ClassCompiler {
    
    /**
     * 编译源码，返回编译后的class字节数据
     * 返回null，表示编译失败
     * </br> 错误信息会放到out中。
     */
    public byte[] compile(String code, PrintWriter out, List<String> options) {
        if (null == out) {
            return null;
        }
        
        if (empty(code)) {
            out.print("Java code can't empty.");
            return null;
        }
        
        String fullClassName = getClassName(code);
        if (null == fullClassName) {
            out.print("The class full name can't be found from the code, for the java code format is not corrent.");
            return null;
        }
        
        if (null == options) {
            options = new ArrayList<String>();
        }
        
        try {
            //获取系统编译器 
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            
            // 建立DiagnosticCollector对象, 用于搜集编辑期间的错误信息
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            
            // 建立用于保存被编译文件名的对象 
            // 每个文件被保存在一个从JavaFileObject继承的类中 
            ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));
            
            //設置需要被編譯的源碼
            List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
            jfiles.add(new SourceJavaFileObject(fullClassName, code));
            
            JavaCompiler.CompilationTask task = compiler.getTask(out, fileManager, diagnostics, options, null, jfiles);
            
            // 编译源程序
            boolean success = task.call();
            if (success) {
                //如果编译成功,用类加载器加载该类 
                ClassJavaFileObject classJavaFileObject = fileManager.getClassJavaFileObject();
                if (null == classJavaFileObject) {
                    out.println("Failed to compile class.");
                    outputErrorMsg(diagnostics, out);
                    return null;
                }
                
                return classJavaFileObject.getClassBytes();
            }
            else {
                outputErrorMsg(diagnostics, out);
                return null;
            }
        }
        catch (Throwable t) {
            t.printStackTrace(out);
        }
        
        return null;
    }
    
    private void outputErrorMsg(DiagnosticCollector<JavaFileObject> diagnostics, PrintWriter out) {
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            outputErrorMsg(diagnostic, out);
        }
    }

    private void outputErrorMsg(Diagnostic<? extends JavaFileObject> diagnostic, PrintWriter out) {
        out.println("Code:[" + diagnostic.getCode() + "]");
        out.println("Kind:[" + diagnostic.getKind() + "]");
        out.println("Position:[" + diagnostic.getPosition() + "]");
        out.println("Start Position:[" + diagnostic.getStartPosition() + "]");
        out.println("End Position:[" + diagnostic.getEndPosition() + "]");
        out.println("Source:[" + diagnostic.getSource() + "]");
        out.println("Message:[" + diagnostic.getMessage(null) + "]");
        out.println("LineNumber:[" + diagnostic.getLineNumber() + "]");
        out.println("ColumnNumber:[" + diagnostic.getColumnNumber() + "]");
    }
    
    /**
     * 获取类的全名称
     * </br> 获取不到，返回null
     */
    private String getClassName(String code) {
        //get package
        String packageName = code.substring(code.indexOf("package") + 7, code.indexOf(";"));
        packageName = trim(packageName);
        
        //get class simple name
        String simpleName = code.substring(code.indexOf(" class ") + 7, code.indexOf("{"));
        simpleName = trim(simpleName);
        
        if (empty(simpleName)) {
            return null;
        }
        
        if (empty(packageName)) {
            return simpleName;
        }
        else {
            return packageName + "." + simpleName;
        }
    }
    
    private boolean empty(String str) {
        return null == str || str.isEmpty();
    }
    
    private String trim(String str) {
        if (null == str) {
            return str;
        }
        
        return str.trim();
    }
}
