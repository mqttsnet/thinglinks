package com.mqttsnet.thinglinks.msg.glue;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.msg.glue.impl.SpringGlueFactory;
import com.mqttsnet.thinglinks.msg.strategy.MsgStrategy;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.transform.TimedInterrupt;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * glue factory, product class/object by name
 *
 * @author mqttsnet
 */
public class GlueFactory {

    private static final Logger log = LoggerFactory.getLogger(GlueFactory.class);

    private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static GlueFactory glueFactory = new SpringGlueFactory();

    /**
     * 脚本中禁止调用的方法名：进程执行、反射、类加载、JVM 控制等 RCE 入口。
     */
    private static final Set<String> BLOCKED_METHODS = new HashSet<>(Arrays.asList(
            "execute", "exec", "getRuntime", "forName", "newInstance", "loadClass", "defineClass",
            "getClassLoader", "getMetaClass", "setMetaClass", "invokeMethod", "getMethod", "getMethods",
            "getDeclaredMethod", "getDeclaredMethods", "getConstructor", "getConstructors",
            "getDeclaredConstructor", "getDeclaredConstructors", "getField", "getDeclaredField",
            "getDeclaredFields", "setAccessible", "exit", "halt", "loadLibrary", "getenv",
            "getProperties", "setSecurityManager", "evaluate", "parseClass",
            // Spring 容器后门：脚本不应通过 applicationContext 拿任意 bean / 改环境 / 发事件
            "getBean", "getBeansOfType", "getBeanNamesForType", "getBeanFactory",
            "getAutowireCapableBeanFactory", "getEnvironment", "publishEvent"));

    /**
     * 脚本中禁止引用/构造的类型（简单名比对）：进程、反射、文件、类加载、脚本引擎等。
     */
    private static final Set<String> BLOCKED_TYPES = new HashSet<>(Arrays.asList(
            "Runtime", "ProcessBuilder", "Process", "Class", "ClassLoader",
            "Thread", "ThreadGroup", "File", "Files", "Paths", "FileInputStream",
            "FileOutputStream", "FileReader", "FileWriter", "RandomAccessFile",
            "Method", "Field", "Constructor", "GroovyShell", "GroovyClassLoader",
            "Eval", "ScriptEngine", "ScriptEngineManager", "Unsafe",
            "ProcessGroovyMethods", "DefaultGroovyMethods", "DefaultGroovyStaticMethods",
            "URLClassLoader", "GroovyScriptEngine", "CompilerConfiguration", "Socket",
            "ServerSocket", "DatagramSocket", "ObjectInputStream"));

    /**
     * 源码级危险 Token 黑名单。先剥离字符串字面量和注释，再扫描剩余代码结构，补上
     * SecureASTCustomizer/AST 遍历对动态 Groovy 调用、字符串插值拼接等场景的盲区。
     */
    private static final Set<String> DANGEROUS_TOKENS = new HashSet<>(Arrays.asList(
            "Runtime", "ProcessBuilder", "Process",
            "File", "FileWriter", "FileReader", "FileInputStream", "FileOutputStream",
            "BufferedReader", "BufferedWriter", "InputStreamReader", "OutputStreamWriter",
            "RandomAccessFile", "FileDescriptor",
            "Class.forName", "getMethod", "getDeclaredMethod", "getDeclaredConstructor",
            "getDeclaredField", "getConstructor", "invoke", "newInstance",
            "setAccessible", "getDeclaredFields", "getDeclaredMethods",
            "ClassLoader", "URLClassLoader", "GroovyClassLoader", "GroovyShell",
            "GroovyScriptEngine", "CompilerConfiguration", "defineClass",
            "metaClass", "ExpandoMetaClass", "Expando",
            "@ASTTest", "@Grab", "@GrabConfig",
            "System.exit", "System.getenv", "System.setProperty",
            "System.getProperties", "System.load", "System.loadLibrary",
            "Socket", "ServerSocket", "DatagramSocket",
            "ObjectInputStream", "readObject", "readUnshared",
            "execute"
    ));

    /**
     * 危险调用模式。这里不把裸 Token "exec" 加入源码黑名单，因为消息策略接口方法
     * 本身就是 MsgStrategy.exec(MsgParam)，但仍禁止 .exec(...) 这类进程执行入口。
     */
    private static final Pattern[] DANGEROUS_CALL_PATTERNS = {
            Pattern.compile("\\.execute\\s*\\("),
            Pattern.compile("\\.exec\\s*\\("),
            Pattern.compile("\\$\\{.*\\b(execute|exec|Runtime|Process|File|invoke|newInstance)\\b"),
            Pattern.compile("\\.forName\\s*\\("),
            Pattern.compile("@\\s*(?:groovy\\.transform\\.)?ASTTest"),
            Pattern.compile("@\\s*Grab\\b")
    };

    /**
     * 编译期 AST 转换（@ASTTest/@Grab）会在 parseClass 时即执行其动作，必须在编译前源码层拦截。
     */
    private static final Pattern FORBIDDEN_SOURCE = Pattern.compile(
            "@\\s*(?:groovy\\.transform\\.)?ASTTest"
            + "|@\\s*Grab(?:Config|Resolver|Exclude)?\\b"
            + "|groovy\\.transform\\.ASTTest"
            + "|groovy\\.grape");

    /**
     * groovy class loader（挂载安全沙箱，编译期全量 AST 遍历，防 RCE）
     */
    private final GroovyClassLoader groovyClassLoader =
            new GroovyClassLoader(GlueFactory.class.getClassLoader(), secureConfiguration());

    public static GlueFactory getInstance() {
        return glueFactory;
    }

    public static void refreshInstance(int type) {
        if (type == 0) {
            glueFactory = new GlueFactory();
        } else if (type == 1) {
            glueFactory = new SpringGlueFactory();
        }
    }

    /**
     * 构建带安全沙箱的编译配置：自定义 {@link CompilationCustomizer} 对整个 ClassNode 做全量 AST 遍历
     * （覆盖方法体、static 初始化块、字段初始化、构造器、闭包），拦截危险方法调用、动态方法名、危险类型
     * 引用/构造与方法指针；配合 {@link #assertSourceAllowed(String)} 的编译期 AST 转换源码预扫描，
     * 阻断通知策略脚本执行 OS 命令、反射、文件、类加载等 RCE；正常的消息渲染/取值脚本不受影响。
     */
    /** 脚本执行超时上限（秒）：安全保险，防 while(true) 等 CPU 死循环拖死线程。 */
    private static final long SCRIPT_TIMEOUT_SECONDS = 5L;

    private static CompilerConfiguration secureConfiguration() {
        CompilerConfiguration config = new CompilerConfiguration();
        config.addCompilationCustomizers(new SecurityCustomizer());
        // 执行超时保险：@TimedInterrupt 编译期给所有循环/方法插入超时检查，纯 CPU 死循环也能超时抛
        // TimeoutException（由编译器注入，不受"脚本源码 AST 注解预扫描"限制）。
        config.addCompilationCustomizers(new ASTTransformationCustomizer(
                Collections.singletonMap("value", SCRIPT_TIMEOUT_SECONDS), TimedInterrupt.class));
        return config;
    }

    private static void assertSourceAllowed(String code) {
        if (code != null && FORBIDDEN_SOURCE.matcher(code).find()) {
            throw new SecurityException("脚本安全校验未通过：禁止使用编译期 AST 转换注解（如 @ASTTest / @Grab）");
        }
    }

    private static final class SecurityCustomizer extends CompilationCustomizer {
        SecurityCustomizer() {
            super(CompilePhase.CANONICALIZATION);
        }

        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            classNode.visitContents(new SecurityVisitor(source));
        }
    }

    private static final class SecurityVisitor extends ClassCodeVisitorSupport {
        private final SourceUnit sourceUnit;

        SecurityVisitor(SourceUnit sourceUnit) {
            this.sourceUnit = sourceUnit;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }

        private static void deny(String what) {
            throw new SecurityException("脚本安全校验未通过，禁止使用：" + what);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            String method = call.getMethodAsString();
            if (method == null) {
                deny("动态方法名调用");
            } else if (BLOCKED_METHODS.contains(method)) {
                deny("方法 " + method + "()");
            }
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
            if (BLOCKED_METHODS.contains(call.getMethod())
                    || BLOCKED_TYPES.contains(call.getOwnerType().getNameWithoutPackage())) {
                deny("静态调用 " + call.getOwnerType().getNameWithoutPackage() + "." + call.getMethod() + "()");
            }
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            if (BLOCKED_TYPES.contains(call.getType().getNameWithoutPackage())) {
                deny("构造 " + call.getType().getNameWithoutPackage());
            }
            super.visitConstructorCallExpression(call);
        }

        @Override
        public void visitClassExpression(ClassExpression expression) {
            if (BLOCKED_TYPES.contains(expression.getType().getNameWithoutPackage())) {
                deny("类型 " + expression.getType().getNameWithoutPackage());
            }
            super.visitClassExpression(expression);
        }

        @Override
        public void visitMethodPointerExpression(MethodPointerExpression expression) {
            String method = expression.getMethodName() == null ? null : expression.getMethodName().getText();
            if (method == null || BLOCKED_METHODS.contains(method)) {
                deny("方法指针 &" + method);
            }
            super.visitMethodPointerExpression(expression);
        }
    }

    /**
     * 计算SHA256哈希值（Hex格式）
     *
     * @param input 输入字符串
     * @return {@link String} SHA256哈希值（Hex格式）
     * @throws Exception
     */
    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

    /**
     * 加载groovy脚本，并实例化
     *
     * @param script groovy脚本
     * @return
     * @throws Exception
     */
    public MsgStrategy loadNewInstance(String script) throws Exception {
        if (script != null && !script.trim().isEmpty()) {
            checkScriptSafety(script);
            Class<?> clazz = getCodeSourceClass(script);
            if (clazz != null) {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                if (instance instanceof MsgStrategy inst) {
                    this.injectService(inst);
                    return inst;
                } else {
                    throw new IllegalArgumentException("glue 加载失败，"
                                                       + "无法将实例转换 [" + instance.getClass() + "] 为 MsgStrategy");
                }
            }
        }
        throw new IllegalArgumentException("脚本不能为空");
    }

    /**
     * 执行脚本
     *
     * @param script script
     * @param params params
     * @return java.lang.Object
     * @author mqttsnet
     * @date 2022/7/25 9:35 PM
     * @create [2022/7/25 9:35 PM ] [mqttsnet] [初始创建]
     * @update [2022/7/25 9:35 PM ] [mqttsnet] [变更描述]
     */
    public Object exeGroovyScript(String script, Map<String, Object> params) {
        if (script != null && !script.trim().isEmpty()) {
            checkScriptSafety(script);
            Class<?> clazz = getCodeSourceClass(script);
            if (clazz != null) {
                return InvokerHelper.createScript(clazz, new Binding(params)).run();
            }
        }
        throw BizException.wrap("脚本不能为空");
    }

    private Class<?> getCodeSourceClass(String codeSource) {
        // 编译前源码预扫描：拦截 @ASTTest/@Grab 等编译期 AST 转换（编译即执行）
        assertSourceAllowed(codeSource);
        String hashKey;
        try {
            hashKey = sha256Hex(codeSource);
        } catch (Exception e) {
            // 哈希失败仅跳过缓存，仍走带沙箱的编译
            return groovyClassLoader.parseClass(codeSource);
        }
        Class<?> clazz = CLASS_CACHE.get(hashKey);
        if (clazz == null) {
            // 沙箱若拒绝会在 parseClass 抛出（MultipleCompilationErrorsException），自然上抛、不缓存
            clazz = groovyClassLoader.parseClass(codeSource);
            CLASS_CACHE.putIfAbsent(hashKey, clazz);
        }
        return clazz;
    }

    /**
     * 脚本安全校验：源码 Token 扫描 + 危险调用正则，与 AST 编译期拦截形成多层防御。
     *
     * @param script 脚本源码
     */
    private static void checkScriptSafety(String script) {
        String cleaned = stripStringLiterals(script);
        for (String token : DANGEROUS_TOKENS) {
            if (containsDangerousToken(cleaned, token)) {
                log.warn("Groovy 脚本安全校验未通过, 检测到危险 Token: {}", token);
                throw new SecurityException("脚本包含不允许的危险操作: " + token);
            }
        }
        for (Pattern pattern : DANGEROUS_CALL_PATTERNS) {
            if (pattern.matcher(script).find()) {
                log.warn("Groovy 脚本安全校验未通过, 匹配到危险模式: {}", pattern.pattern());
                throw new SecurityException("脚本包含不允许的危险操作: " + pattern.pattern());
            }
        }
    }

    private static boolean containsDangerousToken(String code, String token) {
        if (token.indexOf('.') >= 0 || token.indexOf('@') >= 0) {
            return code.contains(token);
        }
        return Pattern.compile("(?<![A-Za-z0-9_$])" + Pattern.quote(token) + "(?![A-Za-z0-9_$])")
                .matcher(code)
                .find();
    }

    /**
     * 剥离 Groovy 脚本中的字符串字面量和注释，仅保留代码结构用于 Token 扫描。
     */
    private static String stripStringLiterals(String script) {
        String result = script.replaceAll("\"\"\"[\\s\\S]*?\"\"\"", " ");
        result = result.replaceAll("'''[\\s\\S]*?'''", " ");
        result = result.replaceAll("\"(?:[^\"\\\\]|\\\\.)*\"", " ");
        result = result.replaceAll("'(?:[^'\\\\]|\\\\.)*'", " ");
        result = result.replaceAll("//[^\\n]*", " ");
        result = result.replaceAll("/\\*[\\s\\S]*?\\*/", " ");
        return result.replaceAll("\\s+", " ");
    }

    /**
     * 注入bean字段
     *
     * @param instance
     */
    public void injectService(Object instance) {
        // do something
    }

}
