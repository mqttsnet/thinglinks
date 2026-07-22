package com.mqttsnet.thinglinks.productversion.controller;

import java.util.List;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffVO;
import com.mqttsnet.thinglinks.productversion.vo.query.ProductVersionPageQuery;
import com.mqttsnet.thinglinks.productversion.vo.result.ProductVersionResultVO;
import com.mqttsnet.thinglinks.productversion.vo.result.ProductVersionStatisticsResultVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPublishVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPurgeVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionRollbackVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionSaveVO;
import com.mqttsnet.thinglinks.productversion.vo.update.ProductVersionUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品物模型版本前端控制器。继承 {@link SuperController} 得标准 CRUD(save 仅占位,实际创建走 /publish;
 * update 仅改 remark);业务动作发布 / 回滚 / 历史清理 / 版本 diff 单独定义。
 *
 * @author mqttsnet
 * @see ProductVersionService
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/productVersion")
@Tag(name = "产品物模型版本")
public class ProductVersionController extends SuperController<ProductVersionService, Long, ProductVersion,
    ProductVersionSaveVO, ProductVersionUpdateVO, ProductVersionPageQuery, ProductVersionResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    /**
     * 按产品标识查询全部版本(created_time 倒序),主动触发 echoService.action 回填字典 / 用户 / 组织展示值。
     */
    @Operation(summary = "按产品查询版本列表")
    @GetMapping("/listByProduct")
    public R<List<ProductVersionResultVO>> listByProduct(@RequestParam String productIdentification) {
        List<ProductVersion> list = superService.listByProductIdentification(productIdentification);
        List<ProductVersionResultVO> result = BeanPlusUtil.toBeanList(list, ProductVersionResultVO.class);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 查询产品当前草稿版本。
     *
     * @return {@link R} 当前草稿版本;无草稿返回 null
     */
    @Operation(summary = "查询产品当前草稿版本")
    @GetMapping("/currentDraft")
    public R<ProductVersionResultVO> currentDraft(@RequestParam String productIdentification) {
        ProductVersionResultVO vo = superService.findDraft(productIdentification)
            .map(v -> BeanPlusUtil.toBeanIgnoreError(v, ProductVersionResultVO.class))
            .orElse(null);
        if (vo != null) {
            echoService.action(vo);
        }
        return R.success(vo);
    }

    /**
     * 按业务键 (productIdentification, versionNo) 反查单个版本行(含完整 productSnapshotJson),
     * 供前端版本预览弹窗回源取快照。URL 不能用 {@code /detail} 否则与父类 {@link SuperController#getDetail} 冲突。
     *
     * @param versionNo 版本序号(雪花标识)
     * @return {@link R} 版本行;不存在返回 null
     */
    @Operation(summary = "按产品+版本号查询版本详情")
    @GetMapping("/detailByVersionNo")
    public R<ProductVersionResultVO> detailByVersionNo(@RequestParam String productIdentification,
                                                       @RequestParam String versionNo) {
        ProductVersionResultVO vo = superService
            .findByProductIdentificationAndVersionNo(productIdentification, versionNo)
            .map(v -> BeanPlusUtil.toBeanIgnoreError(v, ProductVersionResultVO.class))
            .orElse(null);
        if (vo != null) {
            echoService.action(vo);
        }
        return R.success(vo);
    }

    /**
     * 发布新版本。
     *
     * @return {@link R} 新版本行
     */
    @Operation(summary = "发布新版本")
    @PostMapping("/publish")
    @WebLog("发布产品版本")
    public R<ProductVersion> publish(@RequestBody @Valid ProductVersionPublishVO vo) {
        try {
            return R.success(superService.publish(vo));
        } catch (BizException be) {
            return R.fail(be);
        }
    }

    /**
     * 回滚到历史版本。
     *
     * @return {@link R} 目标版本行
     */
    @Operation(summary = "回滚版本")
    @PostMapping("/rollback")
    @WebLog("回滚产品版本")
    public R<ProductVersion> rollback(@RequestBody @Valid ProductVersionRollbackVO vo) {
        try {
            return R.success(superService.rollback(vo));
        } catch (BizException be) {
            return R.fail(be);
        }
    }

    /**
     * 历史清理:删除指定版本的 TD 资源,版本元数据保留。
     *
     * @return {@link R} 已清理版本行
     */
    @Operation(summary = "历史清理")
    @PostMapping("/purgeHistory")
    @WebLog("清理产品历史版本 TD 资源")
    public R<ProductVersion> purgeHistory(@RequestBody @Valid ProductVersionPurgeVO vo) {
        try {
            return R.success(superService.purgeHistory(vo));
        } catch (BizException be) {
            return R.fail(be);
        }
    }

    /**
     * 计算两版本字段级 diff。
     *
     * @param sourceVersion 源版本号(可空)
     * @return {@link R} 差异结果
     */
    @Operation(summary = "版本 diff")
    @GetMapping("/diff")
    public R<ProductVersionDiffVO> diff(@RequestParam String productIdentification,
                                        @RequestParam(required = false) String sourceVersion,
                                        @RequestParam String targetVersion) {
        try {
            return R.success(superService.diff(productIdentification, sourceVersion, targetVersion));
        } catch (BizException be) {
            return R.fail(be);
        }
    }

    /**
     * 物模型版本管理总览统计 ── 跨产品聚合 5 个指标。
     */
    @Operation(summary = "版本管理总览统计")
    @GetMapping("/statistics")
    public R<ProductVersionStatisticsResultVO> statistics() {
        return R.success(superService.statistics());
    }
}
