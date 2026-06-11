package com.mqttsnet.thinglinks.video.controller.admin;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.video.gb28181.session.SsrcPoolService;
import com.mqttsnet.thinglinks.video.service.ssrc.SsrcPoolReconcileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SSRC 池管理接口。
 * <p>
 * 紧急恢复场景使用，普通业务流程不应调用。包含：
 * <ul>
 *   <li>手动触发孤儿对账（补偿定时任务）</li>
 *   <li>查看池可用容量（运维监控）</li>
 *   <li>强制重置池（nuclear，清空全部分配 + 事务）</li>
 * </ul>
 *
 * @author mqttsnet
 */
@Slf4j
@RestController
@RequestMapping("/admin/video/ssrc")
@RequiredArgsConstructor
@Tag(name = "SSRC池管理", description = "SSRC 池孤儿对账、可用量查询、紧急重置")
public class SsrcAdminController {

    private final SsrcPoolReconcileService ssrcPoolReconcileService;
    private final SsrcPoolService ssrcPoolService;

    /**
     * 手动触发对账。
     */
    @Operation(summary = "手动对账", description = "即时对指定流媒体服务器执行孤儿 SSRC 回收，集群中只有抢到锁的节点会实际执行")
    @PostMapping("/reconcile/{mediaIdentification}")
    public R<SsrcPoolReconcileService.ReconcileResult> reconcile(
        @Parameter(description = "流媒体服务器标识") @PathVariable String mediaIdentification) {
        log.info("[管理接口] 手动触发 SSRC 对账: mediaIdentification={}", mediaIdentification);
        return R.success(ssrcPoolReconcileService.reconcile(mediaIdentification));
    }

    /**
     * 紧急重置：清空 SSRC 池 + 关联 SsrcTransaction，再重新初始化池。
     * 会造成设备侧未收到 BYE 的"幽灵会话"，仅在池完全不可恢复时使用。
     */
    @Operation(summary = "紧急重置", description = "【nuclear】清空 SSRC 池 + 关联事务 + 重新初始化池，仅用于恢复异常")
    @PostMapping("/reset/{mediaIdentification}")
    public R reset(@Parameter(description = "流媒体服务器标识") @PathVariable String mediaIdentification) {
        log.warn("[管理接口] 紧急重置 SSRC 池: mediaIdentification={}", mediaIdentification);
        // resetAndReinit 需要 SIP domain 配置重新生成 SSRC 集合，放在协议层 SsrcPoolService
        ssrcPoolService.resetAndReinit(mediaIdentification);
        return R.success();
    }

    /**
     * 查询池可用容量。
     */
    @Operation(summary = "池可用量", description = "返回指定流媒体服务器当前可用 SSRC 数量")
    @GetMapping("/available/{mediaIdentification}")
    public R<Integer> availableCount(
        @Parameter(description = "流媒体服务器标识") @PathVariable String mediaIdentification) {
        return R.success(ssrcPoolService.getAvailableCount(mediaIdentification));
    }
}
