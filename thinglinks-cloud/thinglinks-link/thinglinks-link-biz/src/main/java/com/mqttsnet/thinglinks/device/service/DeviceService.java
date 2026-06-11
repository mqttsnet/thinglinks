package com.mqttsnet.thinglinks.device.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAuthenticationQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceDetailsPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceSslTestQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceSslTestResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceOverviewResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceVersionResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceUpdateVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAuthenticationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoQueryDeviceResultVO;


/**
 * <p>
 * 业务接口
 * 设备档案信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
public interface DeviceService extends SuperService<Long, Device> {

    /**
     * 分页查询设备档案信息
     *
     * @param params 分页参数
     * @return 设备信息分页结果
     */
    IPage<DeviceResultVO> getPage(PageParams<DevicePageQuery> params);

    /**
     * 获取设备档案总量
     *
     * @return 设备档案总数
     */
    Long findDeviceTotal();

    /**
     * 客户端认证
     *
     * @param deviceAuthenticationQuery 客户端认证请求
     * @return 认证结果
     */
    DeviceAuthenticationResultVO authClient(DeviceAuthenticationQuery deviceAuthenticationQuery);

    /**
     * SSL 证书认证测试器 ── 端到端模拟 SSL 双向认证流程,分步返回每个环节结果。
     * 仅给运维端测试器页面使用,不参与设备主认证流程。
     *
     * @param query 测试请求(client 证书 + 可选目标 CA + 可选 clientId)
     * @return SSL 认证测试分步结果
     */
    DeviceSslTestResultVO sslTest(DeviceSslTestQuery query);

    /**
     * 保存设备档案
     *
     * @param saveVO 设备保存数据
     * @return 保存后的设备数据
     */
    DeviceSaveVO saveDevice(DeviceSaveVO saveVO);

    /**
     * 北向API保存设备档案,保存设备并返回完整的设备信息。
     *
     * @param saveVO 设备保存数据
     * @return 完整的设备信息
     */
    DeviceResultVO saveDeviceByNorthbound(DeviceSaveVO saveVO);


    /**
     * 修改设备档案
     *
     * @param updateVO 设备修改数据
     * @return 修改后的设备数据
     */
    DeviceUpdateVO updateDevice(DeviceUpdateVO updateVO);

    /**
     * 根据设备ID更新设备状态
     *
     * @param id     设备 ID
     * @param status 目标状态值
     * @return 更新成功返回 true
     */
    Boolean updateDeviceStatus(Long id, Integer status);

    /**
     * 根据设备ID删除设备
     *
     * @param id 设备 ID
     * @return 删除成功返回 true
     */
    Boolean deleteDevice(Long id);

    /**
     * 批量删除设备(整批事务):N 条在单个事务内逐条删,任一条失败整批回滚。
     * 取代历史 Controller 层 stream().allMatch(deleteDevice) 的"N 个独立事务串行"写法(失败时前面已提交、产生孤儿数据)。
     * 复用 {@link #deleteDevice(Long)} 单条语义逐条调用,缓存清理 / 事件发布 / 下游表清理全走单条路径;
     * 适合常规批删(几十条),超大批量请走分页或异步任务避免长事务锁。
     *
     * @param ids 设备 ID 列表,非空;内部会去重
     * @return 全部成功返回 true;任一失败抛异常 → 整批回滚
     */
    Boolean deleteDevices(java.util.List<Long> ids);


    /**
     * 根据客户端ID查询设备信息
     *
     * @param clientId 客户端 ID
     * @return 设备信息;查不到返 null
     */
    DeviceResultVO findOneByClientId(String clientId);


    /**
     * 根据设备标识查询设备信息(返 {@link DeviceDetailsResultVO})
     *
     * @param deviceIdentification 设备标识
     * @return 设备详情信息;查不到返 null
     */
    DeviceDetailsResultVO findOneByDeviceIdentification(String deviceIdentification);

    /**
     * 根据设备标识查询设备信息(返 {@link DeviceResultVO})
     *
     * @param deviceIdentification 设备标识
     * @return 设备信息;查不到返 null
     */
    DeviceResultVO findByDeviceIdentification(String deviceIdentification);

    /**
     * 修改设备连接状态
     *
     * @param id               设备 ID
     * @param connectionStatus 目标连接状态值
     * @return 修改成功返回 true
     * @throws IllegalArgumentException 如果设备ID或连接状态值无效
     */
    boolean updateDeviceConnectionStatusById(Long id, Integer connectionStatus) throws IllegalArgumentException;

    /**
     * 基于上游事件的连接状态变更 ── event-time LWW CAS。
     * 仅当 device.last_status_event_hlc 严格小于 eventHlc 时才覆盖,防止异步消费 / 乱序 / 抖动重连导致状态回退;
     * 同时更新 last_status_event_hlc 作为下次 CAS 基准。网关设备触发子设备状态联动逻辑与原方法一致。
     *
     * @param clientId 客户端 ID
     * @param status   目标连接状态(0=OFFLINE, 1=ONLINE,由 actionType 推导)
     * @param eventHlc 上游因果时钟 HLC,&gt; 0
     * @return true=CAS 写入生效, false=过期事件被拒绝(DB 已有更新的 hlc)
     */
    boolean updateDeviceConnectionStatusByEvent(String clientId, Integer status, Long eventHlc);


    /**
     * 查询设备信息VO列表
     *
     * @param query 查询条件
     * @return 设备信息列表
     */
    List<DeviceResultVO> getDeviceResultVOList(DevicePageQuery query);

    /**
     * 查询设备信息VO详情列表(包含设备详情、产品)
     *
     * @param query 查询条件
     * @return 设备详情信息列表
     */
    List<DeviceDetailsResultVO> getDeviceDetailsResultVOList(DevicePageQuery query);

    /**
     * 获取设备概述统计信息
     *
     * @return 设备概述统计信息
     */
    DeviceOverviewResultVO getDeviceOverview();

    /**
     * 根据产品标识查询设备软固件版本集合信息
     *
     * @param productIdentification 产品标识
     * @return 设备软固件版本集合信息
     */
    DeviceVersionResultVO getDeviceVersionByProduct(String productIdentification);

    /**
     * MQTT协议下添加子设备
     *
     * @param topoAddSubDeviceParam 添加子设备参数
     * @return 子设备添加结果
     */
    TopoAddDeviceResultVO saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam);

    /**
     * 北向API添加子设备
     *
     * @param topoAddSubDeviceParam 添加子设备参数
     * @return 子设备添加结果
     */
    TopoAddDeviceResultVO saveSubDeviceByNorthbound(TopoAddSubDeviceParam topoAddSubDeviceParam);

    /**
     * MQTT协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新子设备连接状态参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * 北向API更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新子设备连接状态参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO updateSubDeviceConnectStatusByNorthbound(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam);

    /**
     * MQTT协议下删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除子设备参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);


    /**
     * 北向API删除子设备
     *
     * @param topoDeleteSubDeviceParam 删除子设备参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO deleteSubDeviceByNorthbound(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam);

    /**
     * MQTT协议下上报设备数据
     *
     * @param topoDeviceDataReportParam 设备数据上报参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam);


    /**
     * 北向API上报设备数据
     *
     * @param topoDeviceDataReportParam 设备数据上报参数
     * @return 子设备操作结果
     */
    TopoDeviceOperationResultVO deviceDataReportByNorthbound(TopoDeviceDataReportParam topoDeviceDataReportParam);


    /**
     * 根据设备ID查询设备详情
     *
     * @param id 设备 ID
     * @return 设备详情信息
     */
    DeviceDetailsResultVO getDeviceDetails(Long id);

    /**
     * 获取设备详情分页信息
     *
     * @param params 分页参数
     * @return 设备详情分页结果
     */
    IPage<DeviceDetailsResultVO> getDeviceDetailsPage(PageParams<DeviceDetailsPageQuery> params);


    /**
     * 检查是否有设备正在使用该产品(产品删除 / 修改前的占用校验)。
     *
     * @param productIdentification 产品标识
     * @return 存在占用设备返回 true
     * @throws IllegalArgumentException if the productIdentification is null or empty.
     */
    boolean isProductInUseByDevices(String productIdentification);

    /**
     * MQTT协议下查询设备信息
     *
     * @param topoQueryDeviceParam 查询设备参数
     * @return 设备查询结果
     */
    TopoQueryDeviceResultVO queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam);

    /**
     * 北向API查询设备信息
     *
     * @param topoQueryDeviceParam 查询设备参数
     * @return 设备查询结果
     */
    TopoQueryDeviceResultVO queryDeviceByNorthbound(TopoQueryDeviceParam topoQueryDeviceParam);

    /**
     * 上报设备心跳信息
     *
     * @param clientIdentifier 客户端标识
     * @param heartbeatTime    心跳时间(毫秒时间戳),无条件更新 last_heartbeat_time
     * @param eventHlc         事件因果时钟 HLC;非空且 &gt;0 时走 CAS 单调写置 ONLINE,缺失则不动连接状态
     * @return 上报成功返回 true
     */
    Boolean reportDeviceHeartbeat(String clientIdentifier, Long heartbeatTime, Long eventHlc);

    /**
     * 根据 CA 证书序列号统计绑定的设备数量,暴露给跨域调用方(如 cacert 域)。
     *
     * @param certSerialNumber CA 证书序列号
     * @return 绑定的设备数量
     */
    Long countByCertSerialNumber(String certSerialNumber);

    /**
     * 根据 CA 证书序列号统计在线设备数量,暴露给跨域调用方(如 cacert 域)。
     *
     * @param certSerialNumber CA 证书序列号
     * @return 在线设备数量
     */
    Long countOnlineByCertSerialNumber(String certSerialNumber);

    /**
     * 根据 CA 证书序列号查询最近心跳的绑定设备列表(影响面分析用),暴露给跨域调用方(如 cacert 域)。
     *
     * @param certSerialNumber CA 证书序列号
     * @param limit            返回条数上限,&gt; 0
     * @return 设备列表,按最后心跳时间倒序
     */
    List<Device> listTopBoundDevicesByCertSerialNumber(String certSerialNumber, int limit);

    // ────────────── 产品版本发布场景的设备改绑(@DS 切租户库走 service 层,不允许跨域调 Manager)──────────────

    /**
     * 全量改绑:把产品下所有设备 SET 到 toVersion(FULL 发布,不论原绑何版本统一覆盖)。
     * SET 到同一目标值幂等,retry 兜底 job 反复跑也安全。
     *
     * @param productIdentification 产品标识
     * @param toVersion             目标版本号
     * @return 实际改绑设备行数
     */
    int bulkRebindByProduct(String productIdentification, String toVersion);

    /**
     * 白名单改绑:按设备识别码集合 IN 改绑到 toVersion(CANARY 发布命中集合)。
     *
     * @param deviceIdentifications 设备识别码集合(空集合返 0,不做 IO)
     * @param toVersion             目标版本号
     * @return 实际改绑设备行数
     */
    int bulkRebindByDeviceIdentifications(List<String> deviceIdentifications, String toVersion);

    /**
     * 版本切换改绑:把原绑 fromVersion 的设备迁到 toVersion(回滚用,只动当前生效版本、不影响灰度 / 历史版本)。
     * 幂等:重跑时已是 toVersion 不匹配,返 0。
     *
     * @param productIdentification 产品标识
     * @param fromVersion 源版本号(回滚前的当前生效版本)
     * @param toVersion   目标版本号(回滚目标版本)
     * @return 实际改绑设备行数
     */
    int bulkRebindByProductAndVersion(String productIdentification, String fromVersion, String toVersion);
}