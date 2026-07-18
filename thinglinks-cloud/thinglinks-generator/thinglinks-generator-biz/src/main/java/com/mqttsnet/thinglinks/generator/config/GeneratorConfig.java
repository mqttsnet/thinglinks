package com.mqttsnet.thinglinks.generator.config;

import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.generator.enumeration.GenTypeEnum;
import com.mqttsnet.thinglinks.generator.enumeration.ProjectTypeEnum;
import com.mqttsnet.thinglinks.generator.enumeration.SuperClassEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取代码生成相关配置
 *
 * @author mqttsnet
 * @date 2022年3月3日15:05:39
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = GeneratorConfig.PREFIX)
public class GeneratorConfig {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".generator";
    /**
     * 后端工程 thinglinks-datasource-max 代码生成跟路径
     */
    private String outputDir;
    /**
     * 前端工程 thinglinks-web 根路径
     */
    private String frontOutputDir;
    /**
     * 前端工程 thinglinks-web-soybean 根路径
     */
    private String frontSoyOutputDir;
    /**
     * 前端工程thinglinks-web-max-vben 跟路径
     */
    private String frontVben5OutputDir;


    /** 默认项目 */
    private ProjectTypeEnum projectType = ProjectTypeEnum.CLOUD;

    /**
     * 作者
     */
    private String author = "mqttsnet";
    /**
     * 去除表前缀(类名不会包含表前缀)
     */
    private List<String> tablePrefix = new ArrayList<>();
    /**
     * 去除字段前缀
     */
    private List<String> fieldPrefix = new ArrayList<>();
    /**
     * 去除字段后缀
     */
    private List<String> fieldSuffix = new ArrayList<>();
    /**
     * 后端项目前缀
     * 项目部分公共文件夹的名称
     * <p>
     * 如：后端 thinglinks-base、thinglinks-base-api、thinglinks-base-entity、thinglinks-base-biz、thinglinks-base-controller、thinglinks-base-server 等模块的前缀：thinglinks
     * 如：前端 src/api/thinglinks/xxx
     * 如：前端 src/utils/thinglinks/xxx
     * 如：前端 src/utils/thinglinks/xxx
     */
    private String projectPrefix = Constants.PROJECT_PREFIX;

    /**
     * 其他类的父类
     */
    private SuperClassEnum superClass = SuperClassEnum.SUPER_CLASS;

    /**
     * 生成方式
     */
    private GenTypeEnum genType = GenTypeEnum.GEN;

    /**
     * 包配置
     */
    @NestedConfigurationProperty
    private PackageInfoConfig packageInfoConfig = new PackageInfoConfig();

    /**
     * 实体 VO 配置
     */
    @NestedConfigurationProperty
    private EntityConfig entityConfig = new EntityConfig();
    /**
     * Mapper 配置
     */
    @NestedConfigurationProperty
    private MapperConfig mapperConfig = new MapperConfig();
    /**
     * Service 配置
     */
    @NestedConfigurationProperty
    private ServiceConfig serviceConfig = new ServiceConfig();
    /**
     * Manager 配置
     */
    @NestedConfigurationProperty
    private ManagerConfig managerConfig = new ManagerConfig();
    /**
     * Controller 配置
     */
    @NestedConfigurationProperty
    private ControllerConfig controllerConfig = new ControllerConfig();
    /**
     * Web 端配置
     */
    @NestedConfigurationProperty
    private WebVbenConfig webVbenConfig = new WebVbenConfig();
    /** 文件覆盖策略 */
    @NestedConfigurationProperty
    private FileOverrideStrategy fileOverrideStrategy = new FileOverrideStrategy();

    /**
     * 常用的常量、枚举包
     * 配置后，若生成代码时，枚举和常量名称与{constantsPackage.key}一致，就不会重复生成，而是采用{constantsPackage.value}的类
     */
    private Map<String, Class<?>> constantsPackage = new HashMap<>();

}
