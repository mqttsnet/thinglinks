package com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.util.Properties;

/**
 * 自定义的可注入System，功能类同java.lang.System
 * </BR>
 * 该类的作用是，可以通过inject方法来注入System，即修改System的out、in和err的输出输入行为。修改之后的CustomSystem可以通过修改字节码的方式用于替代类原来的java.lang.System类
 * </BR>注入的时候，只是修改in out err的行为，其他System的方法都直接调用java.lang.System对应的方法（该类覆盖了所有java.lang.System的public方法）
 *
 * @author thinglinks
 * @date 2022-07-04
 *
 */
public class InjectionSystem {
    public static InputStream in = System.in;

    public static PrintStream out = System.out;

    public static PrintStream err = System.err;

    private InjectionSystem() {
    }
    
    /**
     * 对System进行注入，即修改in out err三个参数的行为
     *
     *
     * @param iIn 不为null则修改in的行为
     * @param iOut 不为null则修改out的行为
     * @param iErr 不为null则修改err的行为
     */
    public static void inject(InputStream iIn, PrintStream iOut, PrintStream iErr) {
        if (null != in) {
            in = iIn;
        }
        
        if (null != iOut) {
            out = iOut;
        }
        
        if (null != iErr) {
            err = iErr;
        }
    }
    
    /**
     * 还原注入，即恢复为java.lang.System的功能
     */
    public static void restore() {
        in = System.in;
        out = System.out;
        err = System.err;
    }
    
    public static void setIn(InputStream in) {
        System.setIn(in);
    }

    public static void setOut(PrintStream out) {
        System.setOut(out);
    }

    public static void setErr(PrintStream err) {
        System.setErr(err);
    }

    public static Console console() {
        return System.console();
    }

    public static Channel inheritedChannel() throws IOException {
        return System.inheritedChannel();
    }

    public static void setSecurityManager(final SecurityManager s) {
        System.setSecurityManager(s);
    }

    public static SecurityManager getSecurityManager() {
        return System.getSecurityManager();
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long nanoTime() {
        return System.nanoTime();
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static int identityHashCode(Object x) {
        return System.identityHashCode(x);
    }

    public static Properties getProperties() {
        return System.getProperties();
    }

    public static void setProperties(Properties props) {
        System.setProperties(props);
    }

    public static String getProperty(String key) {
        return System.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        return System.getProperty(key, def);
    }

    public static String setProperty(String key, String value) {
        return System.setProperty(key, value);
    }

    public static String clearProperty(String key) {
        return System.clearProperty(key);
    }

    public static String getenv(String name) {
        return System.getenv(name);
    }

    public static java.util.Map<String, String> getenv() {
        return System.getenv();
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static void gc() {
        System.gc();
    }

    public static void runFinalization() {
        System.runFinalization();
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        System.gc();
    }

    public static void load(String filename) {
        System.load(filename);
    }

    public static void loadLibrary(String libname) {
        System.loadLibrary(libname);
    }
}