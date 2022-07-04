package com.mqttsnet.thinglinks.common.core.dynamicCompilation;

import java.io.PrintWriter;
import java.lang.reflect.Method;

/**
 * Class执行器
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class ClassExecutor {

    /**
     * 执行Main函数
     *
     *
     * @param cls 待执行的类
     * @param out 错误信息输出
     * @return 是否执行成功 
     */
    public boolean executeMain(Class<?> cls, PrintWriter out) {
        Method method;
        try {
            method = cls.getMethod("main", new Class[] {String[].class});
            method.invoke(null, new String[] {null});
        }
        catch (Throwable  t) {
            t.printStackTrace(out);
        }
        
        return false;
    }
}
