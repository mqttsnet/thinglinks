package com.mqttsnet.thinglinks.service.plugin.impl;

import cn.hutool.core.net.NetUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.PluginServer;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.plugin.PluginInfo;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginActionStatusEnum;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginInfoStatusEnum;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginLevelEnum;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginRunModeEnum;
import com.mqttsnet.thinglinks.enumeration.plugin.PluginTypeEnum;
import com.mqttsnet.thinglinks.file.facade.FileFacade;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import com.mqttsnet.thinglinks.manager.plugin.PluginInfoManager;
import com.mqttsnet.thinglinks.open.exp.client.Plugin;
import com.mqttsnet.thinglinks.service.plugin.PluginInfoService;
import com.mqttsnet.thinglinks.service.plugin.PluginInstanceMappingService;
import com.mqttsnet.thinglinks.service.plugin.PluginInstanceService;
import com.mqttsnet.thinglinks.service.plugin.PluginScanService;
import com.mqttsnet.thinglinks.utils.SecurityScanUtil;
import com.mqttsnet.thinglinks.vo.query.plugin.PluginInfoPageQuery;
import com.mqttsnet.thinglinks.vo.query.plugin.PluginInstanceMappingPageQuery;
import com.mqttsnet.thinglinks.vo.query.plugin.PluginInstancePageQuery;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInfoDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInfoResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInstanceMappingResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginInstanceResultVO;
import com.mqttsnet.thinglinks.vo.result.plugin.PluginResultVO;
import com.mqttsnet.thinglinks.vo.save.plugin.PluginInfoSaveVO;
import com.mqttsnet.thinglinks.vo.save.plugin.PluginInstanceMappingSaveVO;
import com.mqttsnet.thinglinks.vo.update.plugin.PluginInfoUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.mqttsnet.thinglinks.common.utils.FileUploadUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 业务实现类
 * 插件信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-25 19:05:11
 * @create [2024-08-25 19:05:11] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class PluginInfoServiceImpl extends SuperServiceImpl<PluginInfoManager, Long, PluginInfo> implements PluginInfoService {

    private final PluginScanService pluginScanService;

    private final RestTemplate restTemplate;

    private final FileFacade fileApi;

    private final PluginServer pluginServer;

    private final PluginInstanceService pluginInstanceService;

    private final PluginInstanceMappingService pluginInstanceMappingService;

    /**
     * 将 File 转换为 MultipartFile
     *
     * @param file 要转换的 File 对象
     * @return 转换后的 MultipartFile 对象
     * @throws IOException 如果文件读取失败
     */
    public static MultipartFile convertToMultipartFile(File file) throws IOException {
        return FileUploadUtils.toMultipartFile(file);
    }

    @Override
    public PluginInfoResultVO scanAndSavePluginResult(String pluginIdentification) throws IOException {
        log.info("Starting scan for plugin: {}", pluginIdentification);
        PluginInfo pluginInfo = superManager.findByPluginIdentification(pluginIdentification);
        ArgumentAssert.notNull(pluginInfo, "此插件不存在");

        // 下载并扫描文件
        String reportFilePath = SecurityScanUtil.downloadAndScan(pluginInfo.getFileId());

        if (reportFilePath != null) {
            // 上传报告文件并保存到数据库
            File reportFile = new File(reportFilePath);
            MultipartFile multipartFile = convertToMultipartFile(reportFile);
            FileResultVO uploadResult = fileApi.upload(multipartFile, "pluginScanReport", StrPool.EMPTY, null);

            if (uploadResult != null) {
                Long fileId = uploadResult.getId();
                log.info("Scan report uploaded successfully with file ID: {}", fileId);

                pluginInfo.setScanStatus("SUCCESS");
                pluginInfo.setScanReportFileId(fileId.toString());
                pluginInfo.setScanDate(LocalDateTime.now());
                pluginInfo.setScanSummary("Report generated and uploaded successfully.");

                superManager.save(pluginInfo);

                return buildPluginInfoResultVO(pluginInfo);
            }
        }
        log.error("Security scan failed for plugin: {}", pluginIdentification);
        throw BizException.wrap("Security scan failed");
    }

    @Override
    public Boolean installPlugin(Long id, Long instanceId) {
        log.info("Starting installation for pluginId: {}", id);
        PluginInfo pluginInfo = superManager.getById(id);
        ArgumentAssert.notNull(pluginInfo, "此插件不存在");
        PluginInstanceResultVO pluginInstanceResultVO = pluginInstanceService.getPluginInstanceResultVOById(instanceId);
        ArgumentAssert.notNull(pluginInstanceResultVO, "此实例不存在");
        //插件文件位置URL
        String fileLocation = resolveFileLocation(pluginInfo.getFileId(), id);
        try {
            pluginServer.install(NetUtil.getLocalhostStr(), fileLocation);
            log.info("Plugin installation successful for plugin: {}", id);
            // 保存插件实例及端口映射关系
            savePluginInstanceMapping(pluginInfo, pluginInstanceResultVO);
            return true;
        } catch (Exception e) {
            log.error("Plugin installation failed for plugin: {}. Error: {}", id, e.getMessage(), e);
            // 尝试卸载插件
            unInstallPlugin(id, instanceId);
            throw BizException.wrap("Plugin installation failed.", e);
        }
    }

    @Override
    public Boolean unInstallPlugin(Long id, Long instanceId) {
        log.info("Starting un-installation for pluginId: {}", id);
        PluginInfo pluginInfo = superManager.getById(id);
        ArgumentAssert.notNull(pluginInfo, "此插件不存在");
        PluginInstanceResultVO pluginInstanceResultVO = pluginInstanceService.getPluginInstanceResultVOById(instanceId);
        ArgumentAssert.notNull(pluginInstanceResultVO, "此实例不存在");
        String tenantId = ContextUtil.getTenantIdStr();
        try {
            pluginServer.unInstall(pluginInfo.getPluginIdentification(), tenantId);
            log.info("Plugin un-installation successful for pluginId: {}", id);
            // 删除插件实例及端口映射关系
            pluginInstanceMappingService.deletePluginInstanceMapping(pluginInfo.getPluginIdentification(), pluginInstanceResultVO.getInstanceIdentification());
            return true;
        } catch (Exception e) {
            log.error("Plugin un-installation failed for pluginId: {}. Error: {}", id, e.getMessage(), e);
            throw BizException.wrap("Plugin un-installation failed for pluginId: " + id, e);
        }
    }

    @Override
    public PluginResultVO preloadPlugin(Long id) {
        log.info("Starting preloadPlugin for pluginId: {}", id);

        // 获取插件信息
        PluginInfo pluginInfo = superManager.getById(id);
        ArgumentAssert.notNull(pluginInfo, "此插件不存在");

        // 解析文件位置
        String fileLocation = resolveFileLocation(pluginInfo.getFileId(), id);
        Plugin plugin = null;

        PluginInfoStatusEnum pluginInfoStatusEnum;
        // 尝试预加载插件
        try {
            plugin = pluginServer.preload(NetUtil.getLocalhostStr(), fileLocation);
            log.info("Plugin preloading successful for pluginId: {}", id);
            pluginInfoStatusEnum = PluginInfoStatusEnum.PRELOAD_SUCCEEDED;
        } catch (Exception e) {
            log.error("Plugin preloading failed for pluginId: {}. Error: {}", id, e.getMessage(), e);
            pluginInfoStatusEnum = PluginInfoStatusEnum.PRELOAD_FAILED;
        }

        // 如果预加载成功，进行插件标识符校验
        if (PluginInfoStatusEnum.PRELOAD_SUCCEEDED.equals(pluginInfoStatusEnum)) {
            checkIfPluginExistsWithSameIdentification(plugin.getPluginId(), pluginInfo.getId());
        }

        // 更新插件信息
        updatePluginInfoByPreloadPlugin(pluginInfo.getId(), pluginInfoStatusEnum, plugin);

        // 创建并填充结果对象
        return buildPluginResultVO(plugin);
    }

    private void checkIfPluginExistsWithSameIdentification(String pluginId, Long currentPluginInfoId) {
        long count = superManager.count(
                Wraps.<PluginInfo>lbQ()
                        .eq(PluginInfo::getPluginIdentification, pluginId)
                        .eq(PluginInfo::getStatus, PluginInfoStatusEnum.PRELOAD_SUCCEEDED.getValue())
                        .ne(PluginInfo::getId, currentPluginInfoId)
        );

        // 如果查询结果不为零，说明已有插件标识符相同的插件存在
        if (count > 0) {
            throw BizException.wrap("此插件已存在, 请勿重复加载!");
        }
    }

    /**
     * 构建 PluginResultVO 对象
     *
     * @param plugin 插件对象
     * @return 填充好的 PluginResultVO
     */
    private PluginResultVO buildPluginResultVO(Plugin plugin) {
        PluginResultVO resultVO = new PluginResultVO();
        if (Objects.isNull(plugin)) {
            return resultVO;
        }

        resultVO.setPluginIdentification(plugin.getPluginId());
        resultVO.setPluginCode(plugin.getPluginCode());
        resultVO.setPluginDesc(plugin.getPluginDesc());
        resultVO.setPluginVersion(plugin.getPluginVersion());
        resultVO.setPluginExt(plugin.getPluginExt());
        resultVO.setPluginBootClass(plugin.getPluginBootClass());

        // 处理配置支持列表
        resultVO.setConfigSupportList(
                Optional.ofNullable(plugin.getConfigSupportList())
                        .orElseGet(List::of)
                        .stream()
                        .map(config -> {
                            PluginResultVO.ConfigSupport configSupport = new PluginResultVO.ConfigSupport();
                            configSupport.setKeyName(config.getKeyName());
                            configSupport.setDefaultValue(config.getDefaultValue());
                            configSupport.setDesc(config.getDesc());
                            configSupport.setRequired(config.isRequired());
                            configSupport.setPluginIdentification(resultVO.getPluginIdentification());
                            return configSupport;
                        })
                        .collect(Collectors.toList())
        );
        return resultVO;
    }

    private void updatePluginInfoByPreloadPlugin(Long pluginInfoId, PluginInfoStatusEnum pluginInfoStatusEnum, Plugin plugin) {
        LambdaUpdateWrapper<PluginInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PluginInfo::getId, pluginInfoId)
                .set(PluginInfo::getStatus, pluginInfoStatusEnum.getValue());

        Optional.ofNullable(plugin).ifPresent(p -> {
            updateWrapper.set(PluginInfo::getPluginIdentification, p.getPluginId())
                    .set(PluginInfo::getPluginCode, p.getPluginCode())
                    .set(PluginInfo::getVersion, p.getPluginVersion())
                    .set(PluginInfo::getDescription, p.getPluginDesc())
                    .set(PluginInfo::getExtendParams, p.getPluginExt());
        });

        superManager.update(updateWrapper);
    }

    @Override
    public PluginInfoSaveVO savePlugin(PluginInfoSaveVO saveVO) {
        log.info("savePlugin saveVO:{}", saveVO);
        validateSaveVO(saveVO);

        // 构建插件实体
        PluginInfo plugin = buildPluginSaveVO(saveVO);
        superManager.save(plugin);

        return BeanPlusUtil.copyProperties(plugin, PluginInfoSaveVO.class);
    }

    @Override
    public PluginInfoUpdateVO updatePlugin(PluginInfoUpdateVO updateVO) {
        log.info("updatePlugin updateVO:{}", updateVO);
        validateUpdateVO(updateVO);

        // 构建插件实体（仅更新指定字段）
        Builder<PluginInfo> pluginInfoBuilder = buildPluginUpdateVO(updateVO);
        superManager.updateById(pluginInfoBuilder.build());

        return BeanPlusUtil.copyProperties(pluginInfoBuilder.build(), PluginInfoUpdateVO.class);
    }

    @Override
    public PluginInfoDetailsResultVO getPluginInfoDetails(Long id) {
        // 检查插件ID是否有效
        ArgumentAssert.notNull(id, "Plugin ID cannot be null");

        // 获取插件信息
        PluginInfo plugin = Optional.ofNullable(superManager.getById(id))
                .orElseThrow(() -> BizException.wrap("Plugin not exist"));

        // 将插件信息映射到 VO 类
        PluginInfoDetailsResultVO pluginInfoDetailsResultVO = BeanPlusUtil.toBeanIgnoreError(plugin, PluginInfoDetailsResultVO.class);

        // 查询插件实例映射关系
        PluginInstanceMappingPageQuery pluginInstanceMappingPageQuery = new PluginInstanceMappingPageQuery()
                .setPluginIdentification(plugin.getPluginIdentification());

        // 获取插件实例映射关系，若为空则使用空列表
        List<PluginInstanceMappingResultVO> pluginInstanceMappingResultVOList = Optional.ofNullable(pluginInstanceMappingService.getPluginInstanceMappingResultVOList(pluginInstanceMappingPageQuery))
                .orElse(Collections.emptyList());

        // 按照实例唯一标识分组插件实例映射数据
        Map<String, List<PluginInstanceMappingResultVO>> pluginInstanceMappingGrouped = pluginInstanceMappingResultVOList.stream()
                .collect(Collectors.groupingBy(PluginInstanceMappingResultVO::getInstanceIdentification));

        // 获取与插件实例相关的实例信息
        List<String> instanceIdentificationList = new ArrayList<>(pluginInstanceMappingGrouped.keySet());

        PluginInstancePageQuery pluginInstancePageQuery = new PluginInstancePageQuery().setInstanceIdentificationList(instanceIdentificationList);

        // 获取插件实例数据，若为空则使用空列表
        List<PluginInstanceResultVO> pluginInstanceResultVOList = Optional.ofNullable(pluginInstanceService.getPluginInstanceResultVOList(pluginInstancePageQuery))
                .orElse(Collections.emptyList());

        // 构建插件实例映射及实例详细信息
        List<PluginInfoDetailsResultVO.PluginInstanceDetails> pluginInstances = pluginInstanceMappingGrouped.entrySet().stream()
                .map(entry -> {
                    String instanceIdentification = entry.getKey();
                    List<PluginInstanceMappingResultVO> instanceMappings = entry.getValue();

                    // 获取该实例的详细信息
                    PluginInstanceResultVO instanceInfo = pluginInstanceResultVOList.stream()
                            .filter(instance -> instanceIdentification.equals(instance.getInstanceIdentification()))
                            .findFirst()
                            .orElse(null);

                    // 如果实例存在，将实例信息附加到每个映射
                    if (Objects.nonNull(instanceInfo)) {
                        PluginInfoDetailsResultVO.PluginInstanceDetails instanceVO = buildInstanceDetails(instanceInfo, instanceMappings);
                        return Optional.of(instanceVO);
                    }
                    return Optional.<PluginInfoDetailsResultVO.PluginInstanceDetails>empty();
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        // 设置插件实例信息
        pluginInfoDetailsResultVO.setPluginInstanceDetailsList(pluginInstances);

        return pluginInfoDetailsResultVO;
    }

    /**
     * 构建实例详情信息
     *
     * @param instanceInfo     插件实例信息
     * @param instanceMappings 插件实例的端口映射
     * @return PluginInstanceDetails 实例详情对象
     */
    private PluginInfoDetailsResultVO.PluginInstanceDetails buildInstanceDetails(PluginInstanceResultVO instanceInfo, List<PluginInstanceMappingResultVO> instanceMappings) {
        // 构建实例详情
        PluginInfoDetailsResultVO.PluginInstanceDetails instanceVO = new PluginInfoDetailsResultVO.PluginInstanceDetails();
        instanceVO.setId(instanceInfo.getId());
        instanceVO.setInstanceIdentification(instanceInfo.getInstanceIdentification());
        instanceVO.setInstanceName(instanceInfo.getInstanceName());
        instanceVO.setInstanceIp(instanceInfo.getMachineIp());
        instanceVO.setHealthy(instanceInfo.getHealthy());

        // 通过映射关系构建端口映射列表
        List<PluginInfoDetailsResultVO.PluginInstanceDetails.PortMapping> portMappings = instanceMappings.stream()
                .map(mapping -> new PluginInfoDetailsResultVO.PluginInstanceDetails.PortMapping(
                        mapping.getPort(),
                        mapping.getPortType(),
                        mapping.getStatus()
                ))
                .collect(Collectors.toList());

        instanceVO.setPortMappings(portMappings);
        return instanceVO;
    }

    @Override
    public List<PluginInfoResultVO> getPluginInfoResultVOList(PluginInfoPageQuery query) {
        return BeanPlusUtil.toBeanList(superManager.getPluginInfoList(query), PluginInfoResultVO.class);
    }

    @Override
    public void installOrUninstallPlugin(Long pluginId, Long instanceId, PluginActionStatusEnum status) {
        PluginInfo pluginInfo = superManager.getById(pluginId);
        ArgumentAssert.notNull(pluginInfo, "此插件不存在");
        PluginInstanceResultVO pluginInstanceResultVO = pluginInstanceService.getPluginInstanceResultVOById(instanceId);
        if (PluginActionStatusEnum.INSTALL.equals(status)) {
            installPluginForInstance(pluginInfo, pluginInstanceResultVO);
        } else if (PluginActionStatusEnum.UNINSTALL.equals(status)) {
            uninstallPluginForInstance(pluginInfo, pluginInstanceResultVO);
        }

    }

    @Override
    public Boolean deletePluginInfo(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        PluginInfo pluginInfo = superManager.getById(id);
        if (pluginInfo == null) {
            throw BizException.wrap("此插件不存在");
        }
        // 卸载所有实例中的插件
        uninstallAllPluginsForInstances(pluginInfo.getId());

        // 删除插件信息
        return superManager.removeById(id);
    }

    @Override
    public void uninstallAllPluginsForInstances(Long id) {
        // 获取插件信息
        PluginInfo pluginInfo = superManager.getById(id);
        if (pluginInfo == null) {
            throw BizException.wrap("此插件不存在");
        }
        PluginInstanceMappingPageQuery query = new PluginInstanceMappingPageQuery()
                .setPluginIdentification(pluginInfo.getPluginIdentification());
        List<PluginInstanceMappingResultVO> mappings = pluginInstanceMappingService.getPluginInstanceMappingResultVOList(query);

        ArgumentAssert.notEmpty(mappings, "无插件实例可卸载");

        // 使用 distinct() 确保每个实例只处理一次（不使用 parallelStream，避免 @DS 数据源上下文在 ForkJoinPool 线程中丢失）
        mappings.stream()
                .map(PluginInstanceMappingResultVO::getInstanceIdentification)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(instanceIdentification -> {
                    try {
                        PluginInstanceResultVO pluginInstanceResult = pluginInstanceService.getPluginInstanceResultVO(instanceIdentification);
                        uninstallPluginForInstance(pluginInfo, pluginInstanceResult);
                    } catch (Exception e) {
                        log.error("Failed to uninstall plugin for instance: {}", instanceIdentification, e);
                    }
                });
    }

    /**
     * 安装插件到指定实例
     *
     * @param pluginInfo 插件信息
     * @param instance   插件实例信息
     */
    public void installPluginForInstance(PluginInfo pluginInfo, PluginInstanceResultVO instance) {
        Long pluginId = pluginInfo.getId();
        log.info("Attempting to install plugin {} on instance {}", pluginId, instance.getInstanceIdentification());
        // 校验插件是否已经安装
        if (pluginInstanceMappingService.isPluginInstalledOnInstance(pluginInfo.getPluginIdentification(), instance.getInstanceIdentification())) {
            log.warn("Plugin {} is already installed on instance {}", pluginId, instance.getInstanceIdentification());
            throw new BizException("插件已经安装在实例上,实例唯一标识:" + instance.getInstanceIdentification());
        }
        try {
            // 构建目标实例的 URL，例如：http://<instanceIp>:<port>/installPlugin
            String url = instance.toInetAddrWithHttp() + "/anyUser/ruleOpen/installPlugin";

            // 创建请求参数，tenantId 和 pluginId 应该作为查询参数传递
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("tenantId", ContextUtil.getTenantId())
                    .queryParam("pluginId", pluginId)
                    .queryParam("instanceId", instance.getId());


            // 创建请求体，这里假设插件ID和实例信息作为请求参数传递
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(ContextConstants.TENANT_ID_KEY, ContextUtil.getTenantIdStr());
            HttpEntity<String> entity = new HttpEntity<>(headers);


            log.info("Sending request to URL: {} with query parameters", uriBuilder.toUriString());

            ResponseEntity<R<?>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            log.info("installPluginForInstance Response: {}", Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            log.error("Error installing pluginId {} on instanceIdentification {}: error:{}", pluginId, instance.getInstanceIdentification(), e.getMessage(), e);
            throw new BizException("在实例上安装插件失败,实例唯一标识:" + instance.getInstanceIdentification());
        }
    }

    /**
     * 保存插件实例映射
     *
     * @param pluginInfo             插件信息
     * @param pluginInstanceResultVO 实例信息
     */
    private void savePluginInstanceMapping(PluginInfo pluginInfo, PluginInstanceResultVO pluginInstanceResultVO) {
        PluginInstanceMappingSaveVO pluginInstanceMappingSaveVO = new PluginInstanceMappingSaveVO();
        pluginInstanceMappingSaveVO.setPluginIdentification(pluginInfo.getPluginIdentification());
        pluginInstanceMappingSaveVO.setInstanceIdentification(pluginInstanceResultVO.getInstanceIdentification());
        pluginInstanceMappingSaveVO.setPort(Integer.valueOf(pluginInstanceResultVO.getMachinePort()));
        pluginInstanceMappingSaveVO.setPortType("HTTP");
        pluginInstanceMappingSaveVO.setRemark("使用实例默认端口,系统自动添加,端口号:" + pluginInstanceResultVO.getMachinePort());
        pluginInstanceMappingService.savePluginInstanceMapping(pluginInstanceMappingSaveVO);
    }

    /**
     * 卸载插件从指定实例
     *
     * @param pluginInfo 插件信息
     * @param instance   插件实例信息
     */
    public void uninstallPluginForInstance(PluginInfo pluginInfo, PluginInstanceResultVO instance) {
        Long pluginId = pluginInfo.getId();
        log.info("Attempting to uninstall plugin {} from instance {}", pluginId, instance.getInstanceIdentification());
        try {
            // 构建目标实例的 URL，例如： http://<instanceIp>:<port>/uninstallPlugin
            String url = instance.toInetAddrWithHttp() + "/anyUser/ruleOpen/uninstallPlugin";

            // 创建请求参数，tenantId 和 pluginId 应该作为查询参数传递
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
                    .queryParam("tenantId", ContextUtil.getTenantId())
                    .queryParam("pluginId", pluginId)
                    .queryParam("instanceId", instance.getId());
            ;

            // 创建请求体Header
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(ContextConstants.TENANT_ID_KEY, ContextUtil.getTenantIdStr());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            log.info("Sending request to URL: {} with query parameters", uriBuilder.toUriString());

            ResponseEntity<R<?>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info("uninstallPluginForInstance Response: {}", Objects.requireNonNull(response.getBody()));
        } catch (Exception e) {
            log.error("Error uninstalling pluginId {} from instanceIdentification {}: error:{}", pluginId, instance.getInstanceIdentification(), e.getMessage(), e);
            throw new BizException("在实例上卸载插件失败,实例唯一标识:" + instance.getInstanceIdentification());
        }
    }

    private void updatePluginInfoStatus(Long pluginId, PluginInfoStatusEnum pluginInfoStatusEnum) {
        try {
            LambdaUpdateWrapper<PluginInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PluginInfo::getId, pluginId)
                    .set(PluginInfo::getStatus, pluginInfoStatusEnum.getValue());
            boolean updateSuccess = superManager.update(updateWrapper);
            if (updateSuccess) {
                log.info("Plugin status for pluginId {} updated successfully to {}", pluginId, pluginInfoStatusEnum.getDesc());
            } else {
                log.warn("Failed to update plugin status for pluginId {}", pluginId);
            }
        } catch (Exception e) {
            log.error("Failed to update plugin status for pluginId {}: {}", pluginId, e.getMessage(), e);
        }
    }

    /**
     * 解析文件位置并进行URL编码
     *
     * @param fileId   插件文件ID
     * @param pluginId 插件ID
     * @return 编码后的文件位置字符串
     */
    private String resolveFileLocation(String fileId, Long pluginId) {
        log.info("Resolving file location for pluginId: {}", pluginId);
        Map<Long, String> fileUrlMap;

        try {
            R<Map<Long, String>> urlFromDefById = fileApi.findUrlFromDefById(
                    Stream.of(fileId).map(Long::valueOf).collect(Collectors.toList())
            );
            ArgumentAssert.isFalse(!urlFromDefById.getIsSuccess(), "Failed to get the plugin file, pluginId: {}", pluginId);
            fileUrlMap = urlFromDefById.getData();
            log.info("File URL map obtained: {}", fileUrlMap);
        } catch (Exception e) {
            log.error("Failed to retrieve plugin file URLs for pluginId: {}. Error: {}", pluginId, e.getMessage(), e);
            throw BizException.wrap("Failed to retrieve plugin file URLs.", e);
        }

        return Optional.ofNullable(fileId)
                .map(locations -> Arrays.stream(locations.split(StrPool.COMMA))
                        .map(String::trim)
                        .map(idStr -> {
                            try {
                                return Optional.of(Long.valueOf(idStr));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid file ID encountered: {}", idStr, e);
                                return Optional.<Long>empty();
                            }
                        })
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(fileUrlMap::get)
                        .map(url -> URLEncoder.encode(url, StandardCharsets.UTF_8))
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(StrPool.COMMA)))
                .orElseThrow(() -> {
                    log.error("File location could not be determined for pluginId: {}", pluginId);
                    return BizException.wrap("File location could not be determined.");
                });
    }

    private PluginInfo buildPluginSaveVO(PluginInfoSaveVO saveVO) {
        PluginInfo pluginInfo = BeanPlusUtil.copyProperties(saveVO, PluginInfo.class);
        pluginInfo.setStatus(PluginInfoStatusEnum.UPLOADED.getValue());
        pluginInfo.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return pluginInfo;
    }

    private Builder<PluginInfo> buildPluginUpdateVO(PluginInfoUpdateVO updateVO) {
        return Builder.of(PluginInfo::new)
                .with(PluginInfo::setId, updateVO.getId())
                .with(PluginInfo::setPluginName, updateVO.getPluginName())
                .with(PluginInfo::setAppId, updateVO.getAppId())
                .with(PluginInfo::setFileId, updateVO.getFileId())
                .with(PluginInfo::setFileSize, updateVO.getFileSize())
                .with(PluginInfo::setLicenseType, updateVO.getLicenseType())
                .with(PluginInfo::setLicenseKey, updateVO.getLicenseKey())
                .with(PluginInfo::setValidUntil, updateVO.getValidUntil())
                .with(PluginInfo::setRunMode, updateVO.getRunMode())
                .with(PluginInfo::setLevel, updateVO.getLevel())
                .with(PluginInfo::setType, updateVO.getType())
                .with(PluginInfo::setRemark, updateVO.getRemark())
                .with(PluginInfo::setFileHash, updateVO.getFileHash())
                .with(PluginInfo::setScanStatus, updateVO.getScanStatus())
                .with(PluginInfo::setScanReportFileId, updateVO.getScanReportFileId())
                .with(PluginInfo::setScanDate, updateVO.getScanDate())
                .with(PluginInfo::setScanSummary, updateVO.getScanSummary())
                .with(PluginInfo::setExtendParams, updateVO.getExtendParams())
                .with(PluginInfo::setCreatedOrgId, ContextUtil.getCurrentDeptId());

    }

    private void validateSaveVO(PluginInfoSaveVO saveVO) {
        ArgumentAssert.notNull(saveVO.getLevel(), "level cannot be null");
        PluginLevelEnum.fromValue(saveVO.getLevel()).orElseThrow(() -> new BizException("Invalid plugin level"));

        ArgumentAssert.notNull(saveVO.getType(), "type cannot be null");
        PluginTypeEnum.fromValue(saveVO.getType()).orElseThrow(() -> new BizException("Invalid plugin type"));

        ArgumentAssert.notNull(saveVO.getRunMode(), "runMode cannot be null");
        PluginRunModeEnum.fromValue(saveVO.getRunMode()).orElseThrow(() -> new BizException("Invalid plugin run mode"));

        // 其他业务规则校验

        // 插件代码保证唯一


    }

    private void validateUpdateVO(PluginInfoUpdateVO updateVO) {
        // 校验ID
        ArgumentAssert.notNull(updateVO.getId(), "id cannot be null");
        // 校验枚举值是否有效
        PluginLevelEnum.fromValue(updateVO.getLevel()).orElseThrow(() -> new BizException("Invalid plugin level"));
        PluginTypeEnum.fromValue(updateVO.getType()).orElseThrow(() -> new BizException("Invalid plugin type"));
        PluginRunModeEnum.fromValue(updateVO.getRunMode()).orElseThrow(() -> new BizException("Invalid plugin run mode"));

        // 其他业务规则校验
        PluginInfo existingPluginInfo = Optional.ofNullable(superManager.getById(updateVO.getId()))
                .orElseThrow(() -> BizException.wrap("The plugin does not exist"));

    }

    private PluginInfoResultVO buildPluginInfoResultVO(PluginInfo pluginInfo) {
        return BeanPlusUtil.toBeanIgnoreError(pluginInfo, PluginInfoResultVO.class);
    }

}


