package com.mqttsnet.thinglinks.base.vo.result.user;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.base.entity.TreeEntity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 构建 Vue路由
 *
 * @author mqttsnet
 * @date 2019-10-20 15:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({TreeEntity.LABEL, TreeEntity.SORT_VALUE, TreeEntity.PARENT_ID, Entity.UPDATED_BY, Entity.UPDATED_TIME, Entity.CREATED_TIME, Entity.CREATED_BY, Entity.ID_FIELD})
public class VueRouter extends TreeEntity<VueRouter, Long> implements EchoVO {

    private static final long serialVersionUID = -3327478146308500708L;
    private final Map<String, Object> echoMap = MapUtil.newHashMap();
    @Schema(description = "路径")
    private String path;
    @Schema(description = "网址")
    private String link;
    @Schema(description = "菜单名称")
    private String name;
    /**
     * 菜单资源编码（全局唯一）。
     * <p>用于前端 keep-alive cache key、权限校验、菜单同步等需要唯一标识的场景。
     */
    @Schema(description = "菜单资源编码")
    private String code;
    @Schema(description = "组件")
    private String component;
    @Schema(description = "重定向")
    private String redirect;
    @Schema(description = "元数据")
    private RouterMeta meta;
    @Schema(description = "类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.RESOURCE_TYPE)
    private String resourceType;
    @Schema(description = "打开方式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.RESOURCE_OPEN_WITH)
    private String openWith;

    @JsonIgnore
    private Boolean isHidden;
    @JsonIgnore
    private String metaJson;
    @JsonIgnore
    private String icon;
}
