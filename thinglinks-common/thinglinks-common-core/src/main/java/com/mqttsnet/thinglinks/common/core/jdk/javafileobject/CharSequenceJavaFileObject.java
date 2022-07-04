package com.mqttsnet.thinglinks.common.core.jdk.javafileobject;

import com.mqttsnet.thinglinks.common.core.constant.XCodeConstant;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * 将java源码保存在content属性中
 *
 * @author moqinggen
 * @date 2021/07/01
 */
public class CharSequenceJavaFileObject extends SimpleJavaFileObject {

    /**
     * 源码内容
     */
    private String content;

    /**
     * @param classFullName 类的完全限定名
     * @param content       java代码
     */
    public CharSequenceJavaFileObject(String classFullName, String content) {
        // 示例：string:///com/momo/xcode/TestAction_1624699371112.java
        super(URI.create("string:///" + classFullName.replace(XCodeConstant.DOT, "/")
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
