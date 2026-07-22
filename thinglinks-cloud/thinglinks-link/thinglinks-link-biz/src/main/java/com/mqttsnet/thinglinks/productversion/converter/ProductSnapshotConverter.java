package com.mqttsnet.thinglinks.productversion.converter;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.productcommand.vo.param.ProductCommandParamVO;
import com.mqttsnet.thinglinks.productcommandrequest.vo.param.ProductCommandRequestParamVO;
import com.mqttsnet.thinglinks.productcommandresponse.vo.param.ProductCommandResponseParamVO;
import com.mqttsnet.thinglinks.productproperty.vo.param.ProductPropertyParamVO;
import com.mqttsnet.thinglinks.productservice.enumeration.ProductServiceStatusEnum;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotCommandParameterVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotCommandVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotPropertyVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotServiceVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotVO;
import org.springframework.stereotype.Component;

/**
 * 产品快照转换器,负责 {@link ProductParamVO} ↔ {@link ProductSnapshotVO} ↔ JSON 字符串三向转换。
 *
 * @author mqttsnet
 */
@Component
public class ProductSnapshotConverter {

    /**
     * 业务 VO 转快照 VO。versionRow 用于回填 versionNo / publishTime 等元数据,可选。
     *
     * @param source     业务 VO
     * @param versionRow 版本行,用于回填元数据,可选
     * @return 快照 VO
     */
    public ProductSnapshotVO toSnapshot(ProductParamVO source, ProductVersion versionRow) {
        if (source == null) {
            return ProductSnapshotVO.builder().build();
        }

        ProductSnapshotVO target = BeanPlusUtil.toBeanIgnoreError(source, ProductSnapshotVO.class);

        Optional.ofNullable(versionRow).ifPresent(v -> {
            target.setVersionNo(v.getVersionNo());
            target.setPublishStrategy(v.getPublishStrategy());
            Optional.ofNullable(v.getPublishTime())
                .map(t -> t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .ifPresent(target::setPublishTime);
        });

        target.setServices(toSnapshotServices(source.getServices()));
        return target;
    }

    /**
     * 把快照 VO 序列化为 JSON 字符串。snapshot==null 返 null;序列化失败抛 {@link IllegalStateException}
     * 显性化脏数据,不能让空串静默落库导致后续 deserialize 误判"无快照"。
     *
     * @param snapshot 快照 VO
     * @return JSON 字符串;snapshot 为 null 返 null
     */
    public String serialize(ProductSnapshotVO snapshot) {
        if (snapshot == null) {
            return null;
        }
        // JsonUtil.toJson 内部 catch 失败返空串 ── 在这里显式 fail-fast,避免空串入库
        String json = JsonUtil.toJson(snapshot);
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException(
                "snapshot serialize failed (JsonUtil returned empty), productIdentification="
                    + snapshot.getProductIdentification());
        }
        return json;
    }

    /**
     * 快照 VO 反向转 {@link ProductModelCacheVO}(给 mqs 走"版本快照解析物模型"用)。
     *
     * @param snapshot 快照 VO
     * @return 物模型缓存 VO;snapshot 为 null 返 null
     */
    public ProductModelCacheVO toModelCacheVO(ProductSnapshotVO snapshot) {
        if (snapshot == null) return null;
        ProductModelCacheVO vo = BeanPlusUtil.toBeanIgnoreError(snapshot, ProductModelCacheVO.class);
        // services 节点 BeanPlusUtil.toBeanIgnoreError 不会递归转,手工补一下;
        // 运行时物模型只取启用服务 —— 停用服务虽在快照里(供版本 diff)但不参与设备解析
        List<ProductSnapshotServiceVO> activeServices = Optional.ofNullable(snapshot.getServices())
            .orElse(Collections.emptyList())
            .stream()
            .filter(svc -> Objects.equals(svc.getServiceStatus(),
                ProductServiceStatusEnum.ACTIVATED.getValue()))
            .collect(Collectors.toList());
        vo.setServices(BeanPlusUtil.toBeanList(activeServices, ProductServiceParamVO.class));
        return vo;
    }

    /**
     * JSON 字符串反序列化为快照 VO。json==null/blank 返 {@link Optional#empty()};JSON 非法抛
     * {@link IllegalStateException} 显性化脏数据,否则会被静默走"全部新增"分支。
     *
     * @param json JSON 字符串
     * @return 快照 VO;json 为 null/blank 返 {@link Optional#empty()}
     */
    public Optional<ProductSnapshotVO> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        // JsonUtil.parse 内部 catch 失败返 null ── 在这里显式 fail-fast,避免"脏数据 = 无快照"误判
        ProductSnapshotVO vo = JsonUtil.parse(json, ProductSnapshotVO.class);
        if (vo == null) {
            // 截断 200 字符,日志可读 + 不爆栈
            String preview = json.length() > 200 ? json.substring(0, 200) + "..." : json;
            throw new IllegalStateException("snapshot deserialize failed, jsonPreview=" + preview);
        }
        return Optional.of(vo);
    }

    // ────────────── 私有:子节点深拷贝转换 ──────────────

    private List<ProductSnapshotServiceVO> toSnapshotServices(List<ProductServiceParamVO> source) {
        return Optional.ofNullable(source)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toSnapshotService)
            .collect(Collectors.toList());
    }

    private ProductSnapshotServiceVO toSnapshotService(ProductServiceParamVO source) {
        ProductSnapshotServiceVO svc = BeanPlusUtil.toBeanIgnoreError(source, ProductSnapshotServiceVO.class);
        svc.setProperties(toSnapshotProperties(source.getProperties()));
        svc.setCommands(toSnapshotCommands(source.getCommands()));
        return svc;
    }

    private List<ProductSnapshotPropertyVO> toSnapshotProperties(List<ProductPropertyParamVO> source) {
        return Optional.ofNullable(source)
            .orElse(Collections.emptyList())
            .stream()
            .map(p -> BeanPlusUtil.toBeanIgnoreError(p, ProductSnapshotPropertyVO.class))
            .collect(Collectors.toList());
    }

    private List<ProductSnapshotCommandVO> toSnapshotCommands(List<ProductCommandParamVO> source) {
        return Optional.ofNullable(source)
            .orElse(Collections.emptyList())
            .stream()
            .map(this::toSnapshotCommand)
            .collect(Collectors.toList());
    }

    private ProductSnapshotCommandVO toSnapshotCommand(ProductCommandParamVO source) {
        ProductSnapshotCommandVO cmd = BeanPlusUtil.toBeanIgnoreError(source, ProductSnapshotCommandVO.class);
        cmd.setRequests(toSnapshotRequestParameters(source.getRequests()));
        cmd.setResponses(toSnapshotResponseParameters(source.getResponses()));
        return cmd;
    }

    private List<ProductSnapshotCommandParameterVO> toSnapshotRequestParameters(List<ProductCommandRequestParamVO> source) {
        return Optional.ofNullable(source)
            .orElse(Collections.emptyList())
            .stream()
            .map(p -> BeanPlusUtil.toBeanIgnoreError(p, ProductSnapshotCommandParameterVO.class))
            .collect(Collectors.toList());
    }

    private List<ProductSnapshotCommandParameterVO> toSnapshotResponseParameters(List<ProductCommandResponseParamVO> source) {
        return Optional.ofNullable(source)
            .orElse(Collections.emptyList())
            .stream()
            .map(p -> BeanPlusUtil.toBeanIgnoreError(p, ProductSnapshotCommandParameterVO.class))
            .collect(Collectors.toList());
    }
}
