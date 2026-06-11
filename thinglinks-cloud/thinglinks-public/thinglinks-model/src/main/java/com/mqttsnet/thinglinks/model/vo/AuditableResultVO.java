package com.mqttsnet.thinglinks.model.vo;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 项目级 ResultVO 标准基类 —— 把"审计字段 echo 化 + EchoVO 形态"收敛到一处,子类只写业务字段。
 * 父类 SuperEntity.createdBy / createdOrgId 与 Entity.updatedBy 是泛型字段且源码在外部 jar 不可改,
 * 无法直接标 @Echo,故在此 shadow 这 3 个字段并统一实现 EchoVO,避免每个子类各自重复。
 * id 固定 Long(雪花),非 Long 主键场景直接继承 {@link Entity} 自管 echoMap,不应改本基类。
 *
 * @author mqttsnet
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableResultVO extends Entity<Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典 / 用户 / 组织等回显结果集合 ── EchoService 通过反射把带 @Echo 字段的查回值写入此 Map,
     * key = 字段名,value = 中文展示文本(用户昵称 / 组织名 / 字典 label),前端 echoMapText(record, '字段名') 优先取此处。
     * 抽象基类无 @Builder/@SuperBuilder,直接字段初始化保证默认非 null。
     */
    @Schema(description = "回显结果集合(字典 / 用户 / 组织等翻译值)")
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    /** shadow SuperEntity.createdBy 并挂 {@link Echo},由 EchoService 回填用户昵称到 echoMap.createdBy。 */
    @Schema(description = "创建人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long createdBy;

    /** shadow Entity.updatedBy,回填用户昵称到 echoMap.updatedBy。 */
    @Schema(description = "最后修改人")
    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    private Long updatedBy;

    /**
     * shadow SuperEntity.createdOrgId,回填组织名到 echoMap.createdOrgId。
     * 数据为空(用户未绑定组织 / 系统操作)时 echoService 不写入 echoMap,前端 echoMapText 自动回退到原 ID。
     */
    @Schema(description = "创建人组织")
    @Echo(api = EchoApi.ORG_ID_CLASS)
    private Long createdOrgId;
}
