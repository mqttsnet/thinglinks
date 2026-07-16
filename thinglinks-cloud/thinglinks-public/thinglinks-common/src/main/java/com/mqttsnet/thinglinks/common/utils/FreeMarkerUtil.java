package com.mqttsnet.thinglinks.common.utils;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.crypto.digest.DigestUtil;
import com.mqttsnet.basic.utils.StrPool;
import freemarker.cache.MruCacheStorage;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.TemplateClassResolver;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 模板引擎工具类
 *
 * @author mqttsnet
 * @version v1.0
 * @date 2022/7/25 12:24 PM
 * @create [2022/7/25 12:24 PM ] [mqttsnet] [初始创建]
 */
@Slf4j
public class FreeMarkerUtil {
    private static final Configuration FREEMARKER_CFG;
    private static final StringTemplateLoader SL;


    // 缓存（Key: 模板MD5, Value: 编译后的Template对象）
    private static final ConcurrentHashMap<String, Template> TEMPLATE_CACHE = new ConcurrentHashMap<>(512);
    private static final int MAX_CACHE_SIZE = 10000;

    static {
        FREEMARKER_CFG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        SL = new StringTemplateLoader();
        FREEMARKER_CFG.setBooleanFormat("c");
        FREEMARKER_CFG.setNumberFormat("0.##");
        generateSharedVariable();
        FREEMARKER_CFG.setCacheStorage(new MruCacheStorage(10000, Integer.MAX_VALUE));
        FREEMARKER_CFG.setTemplateUpdateDelayMilliseconds(6000000L);
        TemplateLoader[] loaders = new TemplateLoader[]{SL};
        MultiTemplateLoader mt = new MultiTemplateLoader(loaders);
        FREEMARKER_CFG.setTemplateLoader(mt);
        // 防 SSTI：禁用 ?new(...) 实例化危险类(如 freemarker.template.utility.Execute)与 ?api 反射访问
        FREEMARKER_CFG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        FREEMARKER_CFG.setAPIBuiltinEnabled(false);
    }

    private static void generateSharedVariable() {
        try {
            BeansWrapper wrapper = new BeansWrapper(Configuration.VERSION_2_3_30);
            TemplateHashModel staticModels = wrapper.getStaticModels();
            TemplateHashModel strPool = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.StrPool");
            FREEMARKER_CFG.setSharedVariable("StrPool", strPool);
            TemplateHashModel dateUtils = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.DateUtils");
            FREEMARKER_CFG.setSharedVariable("DateUtils", dateUtils);
            TemplateHashModel argumentAssert = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.ArgumentAssert");
            FREEMARKER_CFG.setSharedVariable("ArgumentAssert", argumentAssert);
            TemplateHashModel beanPlusUtil = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.BeanPlusUtil");
            FREEMARKER_CFG.setSharedVariable("BeanPlusUtil", beanPlusUtil);
            TemplateHashModel collHelper = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.CollHelper");
            FREEMARKER_CFG.setSharedVariable("CollHelper", collHelper);
            TemplateHashModel springUtils = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.SpringUtils");
            FREEMARKER_CFG.setSharedVariable("SpringUtils", springUtils);
            TemplateHashModel strHelper = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.StrHelper");
            FREEMARKER_CFG.setSharedVariable("StrHelper", strHelper);
            TemplateHashModel treeUtil = (TemplateHashModel) staticModels.get("com.mqttsnet.basic.utils.TreeUtil");
            FREEMARKER_CFG.setSharedVariable("TreeUtil", treeUtil);
        } catch (TemplateModelException e) {
            log.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static String generateString(String strTemplate, Map<String, Object> parameters) {
        String templateName = DigestUtil.md5Hex(strTemplate);

        // 确保模板内容已加载
        if (SL.findTemplateSource(templateName) == null) {
            SL.putTemplate(templateName, strTemplate);
        }

        // 从缓存获取或创建模板对象
        Template template = TEMPLATE_CACHE.computeIfAbsent(templateName, k -> {
            try {
                if (TEMPLATE_CACHE.size() >= MAX_CACHE_SIZE) {
                    TEMPLATE_CACHE.clear();
                }
                return FREEMARKER_CFG.getTemplate(templateName, StrPool.UTF8);
            } catch (Exception e) {
                throw new RuntimeException("Template init failed", e);
            }
        });


        StringWriter writer = new StringWriter();
        template.process(parameters, writer);
        return writer.toString();
    }
}
