package com.mqttsnet.thinglinks.common.core.compile.jdk.javafileobject;


import com.mqttsnet.thinglinks.common.core.constant.XCodeConstant;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;


/**
 * @program: thinglinks
 * @description: 将java源码保存在content属性中
 * @packagename: com.mqttsnet.thinglinks.common.core.compile.jdk.javafileobject
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-01 17:56
 **/
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
        // 示例：string:///com/mqttsnet/thinglinks/xcode/TestAction_1624699371112.java
        super(URI.create("string:///" + classFullName.replace(XCodeConstant.DOT, "/")
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return content;
    }
}
