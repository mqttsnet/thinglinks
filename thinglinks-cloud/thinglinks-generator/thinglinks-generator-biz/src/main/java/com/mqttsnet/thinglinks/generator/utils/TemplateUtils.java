package com.mqttsnet.thinglinks.generator.utils;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.mqttsnet.thinglinks.generator.enumeration.PopupTypeEnum;
import com.mqttsnet.thinglinks.generator.enumeration.TemplateEnum;
import com.mqttsnet.thinglinks.generator.enumeration.TplEnum;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板工具类
 *
 * @author mqttsnet
 * @version v1.0
 * @date 2022/4/2 7:36 PM
 * @create [2022/4/2 7:36 PM ] [mqttsnet] [初始创建]
 */
public class TemplateUtils {
    private static final Configuration CONFIGURATION;

    static {
        CONFIGURATION = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        CONFIGURATION.setDefaultEncoding(StandardCharsets.UTF_8.name());
        CONFIGURATION.setClassForTemplateLoading(SourceCodeUtils.class, StringPool.SLASH);
    }

    public static Template getTemplate(String templatePath) throws IOException {
        return CONFIGURATION.getTemplate(templatePath);
    }

    /**
     * 获取模板列表
     *
     * @return 模板列表
     */
    public static List<String> getTemplateList(TemplateEnum template, TplEnum tplType, PopupTypeEnum popupType) {
        List<String> templates = new ArrayList<>();
        if (TemplateEnum.BACKEND.eq(template)) {
            templates.add(GenCodeConstant.TEMPLATE_CONTROLLER);
            templates.add(GenCodeConstant.TEMPLATE_SERVICE);
            templates.add(GenCodeConstant.TEMPLATE_SERVICE_IMPL);
            templates.add(GenCodeConstant.TEMPLATE_MANAGER);
            templates.add(GenCodeConstant.TEMPLATE_MANAGER_IMPL);
            templates.add(GenCodeConstant.TEMPLATE_MAPPER);
            templates.add(GenCodeConstant.TEMPLATE_XML);
            templates.add(GenCodeConstant.TEMPLATE_ENTITY_JAVA);
            templates.add(GenCodeConstant.TEMPLATE_SAVE_VO);
            templates.add(GenCodeConstant.TEMPLATE_UPDATE_VO);
            templates.add(GenCodeConstant.TEMPLATE_RESULT_VO);
            templates.add(GenCodeConstant.TEMPLATE_PAGE_QUERY);
            templates.add(GenCodeConstant.TEMPLATE_SQL);
        } else if (TemplateEnum.WEB_PLUS.eq(template)) {
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_API);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_MODEL);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_LANG_EN);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_LANG_ZH);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_DATA);

            if (TplEnum.TREE.eq(tplType)) {
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_TREE_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_TREE_EDIT);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_TREE_TREE);
            } else if (TplEnum.MAIN_SUB.eq(tplType)) {
                if (PopupTypeEnum.JUMP.eq(popupType)) {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_MAIN_JUMP_EDIT);
                } else {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_MAIN_EDIT);
                }
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_MAIN_INDEX);
                // 从表
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_MAIN_SUB_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_MAIN_SUB_DATA);

            } else {
                if (PopupTypeEnum.JUMP.eq(popupType)) {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_JUMP_EDIT);
                } else {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_EDIT);
                }
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN_SIMPLE_INDEX);
            }
        } else if (TemplateEnum.WEB_SOYBEAN.eq(template)) {
            templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_API);
            templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_MODEL);
            templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_LANG_EN);
            templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_LANG_ZH);

            if (TplEnum.TREE.eq(tplType)) {
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_EDIT);
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_TREE);
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_CRUD);
            } else if (TplEnum.MAIN_SUB.eq(tplType)) {
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_MAIN_INDEX);
            } else {
                if (PopupTypeEnum.JUMP.eq(popupType)) {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_JUMP_EDIT);
                }
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_CRUD);
            }
        } else if (TemplateEnum.WEB_VBEN5.eq(template)) {
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_API);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_MODEL);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_LANG_EN);
            templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_LANG_ZH);

            if (TplEnum.TREE.eq(tplType)) {
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_TREE_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_TREE_EDIT);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_TREE_TREE);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_TREE_CRUD);
            } else if (TplEnum.MAIN_SUB.eq(tplType)) {
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_DATA);
                // 从表
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_SUB_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_SUB_DATA);
            } else {
                if (PopupTypeEnum.JUMP.eq(popupType)) {
                    templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_JUMP_EDIT);
                }
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_INDEX);
                templates.add(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_CRUD);
            }
        }
        return templates;
    }

    /**
     * 获取模板列表
     *
     * @return 模板列表
     */
    public static List<String> getSubTemplateList(TemplateEnum template) {
        List<String> templates = new ArrayList<>();
        if (TemplateEnum.BACKEND.eq(template)) {
            templates.add(GenCodeConstant.TEMPLATE_MANAGER);
            templates.add(GenCodeConstant.TEMPLATE_MANAGER_IMPL);
            templates.add(GenCodeConstant.TEMPLATE_MAPPER);
            templates.add(GenCodeConstant.TEMPLATE_XML);
            templates.add(GenCodeConstant.TEMPLATE_ENTITY_JAVA);
            templates.add(GenCodeConstant.TEMPLATE_SAVE_VO);
            templates.add(GenCodeConstant.TEMPLATE_UPDATE_VO);
        }
        return templates;
    }

}
