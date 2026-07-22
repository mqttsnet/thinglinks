package com.mqttsnet.thinglinks.system.vo.result.system;


import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.base.entity.TreeEntity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;


/**
 * <p>
 * 实体类
 * 地区表
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefAreaResultVO", description = "地区表")
public class DefAreaResultVO extends TreeEntity<DefAreaResultVO, Long> implements Serializable, EchoVO {

    private static final long serialVersionUID = 1L;
    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();
    @Schema(description = "名称")
    protected String name;
    @Schema(description = "父ID")
    protected Long parentId;
    @Schema(description = "排序号")
    protected Integer sortValue;
    @Schema(description = "主键")
    private Long id;
    /**
     * 编码
     */
    @Schema(description = "编码")

    private String code;
    /**
     * 全名
     */
    @Schema(description = "全名")

    private String fullName;
    /**
     * 经度
     */
    @Schema(description = "经度")

    private String longitude;
    /**
     * 维度
     */
    @Schema(description = "维度")

    private String latitude;
    /**
     * 行政级别;[10-国家 20-省份/直辖市 30-地市 40-区县 50-乡镇]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.AREA_LEVEL)
     */
    @Schema(description = "行政级别")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Global.AREA_LEVEL)

    private String level;
    /**
     * 数据来源;[10-爬取 20-新增]	@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.AREA_SOURCE)
     */
    @Schema(description = "数据来源")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.AREA_SOURCE)

    private String source;
    /**
     * 状态
     */
    @Schema(description = "状态")

    private Boolean state;
    /**
     * 树层级
     */
    @Schema(description = "树层级")

    private Integer treeGrade;
    /**
     * 路径
     */
    @Schema(description = "路径")

    private String treePath;

    /** shadow SuperEntity<Long>.createdBy,挂 @Echo 让 echoService 回填用户昵称到 echoMap.createdBy。 */
    @Schema(description = "创建人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long createdBy;

    /** shadow Entity<Long>.updatedBy,挂 @Echo 让 echoService 回填用户昵称到 echoMap.updatedBy。 */
    @Schema(description = "最后修改人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long updatedBy;

    /** 创建人组织,挂 @Echo 让 echoService 回填组织名到 echoMap.createdOrgId。 */
    @Schema(description = "创建人组织")
    @Echo(api = EchoApi.ORG_ID_CLASS)
    private Long createdOrgId;
}
