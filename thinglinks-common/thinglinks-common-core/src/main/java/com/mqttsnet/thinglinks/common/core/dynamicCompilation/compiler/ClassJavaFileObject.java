package com.mqttsnet.thinglinks.common.core.dynamicCompilation.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * class的文件JavaFileObject对象，用于存放JavaCompiler编译得到的Class二进制内容
 * </br>内容存在ByteArrayOutputStream中
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class ClassJavaFileObject extends SimpleJavaFileObject {

    /**
     * ByteArrayOutputStream流是不需要关闭的
     */
    protected final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    public ClassJavaFileObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    /**
     * 获取Class文件的字节数据
     */
    public byte[] getClassBytes() {
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 重写openOutputStream,将我们的输出流交给JavaCompiler,让它将编译好的Class装载进来
     */
    @Override
    public OutputStream openOutputStream() throws IOException {
        return byteArrayOutputStream;
    }
}