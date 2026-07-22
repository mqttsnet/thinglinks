package com.mqttsnet.thinglinks.ota.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.VersionValidator;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.entity.OtaUpgrades;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageSignMethodEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageStatusEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.ota.manager.OtaUpgradeTasksManager;
import com.mqttsnet.thinglinks.ota.manager.OtaUpgradesManager;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradesService;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeTasksPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradesPageQuery;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradesDetailsResultVO;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradesResultVO;
import com.mqttsnet.thinglinks.ota.vo.save.OtaUpgradesSaveVO;
import com.mqttsnet.thinglinks.ota.vo.update.OtaUpgradesUpdateVO;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * OTA升级包
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:36:27
 * @create [2024-01-12 22:36:27] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class OtaUpgradesServiceImpl extends SuperServiceImpl<OtaUpgradesManager, Long, OtaUpgrades> implements OtaUpgradesService {

    private final OtaUpgradeTasksManager otaUpgradeTasksManager;
    /**
     * 写前置校验保留直调 ── 升级包保存 / 更新校验产品存在,必须 DB-fresh。
     */
    private final ProductQueryService productQueryService;
    /**
     * 详情展示读路径走缓存,read-through DB 兜底。
     */
    private final LinkCacheDataHelper linkCacheDataHelper;

    /**
     * Save OTA Upgrade Package
     *
     * @param saveVO 保存参数
     * @return {@link OtaUpgradesSaveVO} 返回结果
     */
    @Override
    public OtaUpgradesSaveVO saveUpgradePackage(OtaUpgradesSaveVO saveVO) {
        log.info("saveUpgradePackage saveVO: {}", saveVO);

        // Validate the saveVO object
        validateOtaUpgradesSaveVO(saveVO);

        // Map the saveVO to your OtaUpgrade entity
        OtaUpgrades otaUpgrade = buildOtaUpgradeFromSaveVO(saveVO);

        // Persist the OtaUpgrade entity using your manager or repository
        superManager.save(otaUpgrade);

        // Map the saved entity back to OtaUpgradesSaveVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesSaveVO.class);
    }

    /**
     * Update OTA Upgrade Package
     *
     * @param updateVO 更新参数
     * @return {@link OtaUpgradesUpdateVO} 返回结果
     */
    @Override
    public OtaUpgradesUpdateVO updateUpgradePackage(OtaUpgradesUpdateVO updateVO) {
        log.info("Updating OTA upgrade package: {}", updateVO);

        // Validate the updateVO object
        validateOtaUpgradesUpdateVO(updateVO);

        //构建参数
        Builder<OtaUpgrades> otaUpgradesBuilder = builderOtaUpgradesUpdateVO(updateVO);

        // Save the updated entity
        superManager.updateById(otaUpgradesBuilder.with(OtaUpgrades::setId, updateVO.getId()).build());

        // Map the updated entity back to OtaUpgradesUpdateVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgradesBuilder.build(), OtaUpgradesUpdateVO.class);

    }

    /**
     * Update OTA Upgrade Package Status
     *
     * @param id     主键
     * @param status 状态
     * @return {@link Boolean} 返回结果
     */
    @Override
    public Boolean updateOtaUpgradeStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "Package ID cannot be null");
        ArgumentAssert.notNull(status, "Status cannot be null");

        // Here you should define your OtaUpgrade entity class which represents your OTA upgrade package
        OtaUpgrades otaUpgrades = superManager.getById(id);
        if (Objects.isNull(otaUpgrades)) {
            throw BizException.wrap("OTA upgrade package does not exist");
        }
        if (status.equals(otaUpgrades.getStatus())) {
            throw BizException.wrap("The OTA upgrade package status is the same as the current status");
        }

        otaUpgrades.setStatus(status);
        return superManager.updateById(otaUpgrades);
    }

    /**
     * Delete OTA Upgrade Package
     *
     * @param id 主键
     * @return {@link Boolean} 返回结果
     */
    @Override
    public Boolean deleteOtaUpgrade(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        OtaUpgrades otaUpgrade = superManager.getById(id);
        if (Objects.isNull(otaUpgrade)) {
            throw BizException.wrap("OTA upgrade package does not exist");
        }

        PageParams<OtaUpgradeTasksPageQuery> params = new PageParams<>();
        params.setModel(new OtaUpgradeTasksPageQuery().setUpgradeId(id));
        if (otaUpgradeTasksManager.getOtaUpgradeTasksPage(params).getTotal() > 0) {
            throw BizException.wrap("OTA upgrade package is in use and cannot be deleted");
        }
        // Additional checks can be added here if necessary
        return superManager.removeById(id);
    }


    /**
     * Converts OTA upgrades entities to view objects based on specified criteria.
     *
     * @param query The {@link OtaUpgradesPageQuery} object containing the search criteria.
     * @return {@link List<OtaUpgradesResultDTO>} A list of OTA upgrade records that match the given query criteria.
     */
    @Override
    public List<OtaUpgradesResultDTO> getOtaUpgradesResultDTOList(OtaUpgradesPageQuery query) {
        List<OtaUpgrades> otaUpgradesList = superManager.getOtaUpgradesList(query);
        return BeanPlusUtil.toBeanList(otaUpgradesList, OtaUpgradesResultDTO.class);
    }

    @Override
    public Optional<OtaUpgradesResultDTO> getByIdOptional(Long id) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        OtaUpgrades otaUpgrades = superManager.getById(id);
        return Optional.of(BeanPlusUtil.toBeanIgnoreError(otaUpgrades, OtaUpgradesResultDTO.class));
    }

    @Override
    public OtaUpgradesDetailsResultVO getUpgradePackageDetails(Long id) {
        ArgumentAssert.notNull(id, "Upgrade package ID cannot be null");
        OtaUpgrades otaUpgrades = superManager.getById(id);
        ArgumentAssert.notNull(otaUpgrades, "OTA upgrade package does not exist");
        OtaUpgradesDetailsResultVO detailsVO = BeanPlusUtil.toBeanIgnoreError(otaUpgrades, OtaUpgradesDetailsResultVO.class);
        // 详情读路径走缓存(read-through 兜底),避免每次详情请求都直查 product 表
        ProductResultVO productResultVO = linkCacheDataHelper
                .getProductCacheVO(otaUpgrades.getProductIdentification())
                .map(p -> BeanPlusUtil.toBeanIgnoreError(p, ProductResultVO.class))
                .orElse(null);
        detailsVO.setProductResult(productResultVO);
        return detailsVO;
    }

    private void validateOtaUpgradesSaveVO(OtaUpgradesSaveVO saveVO) {
        OtaPackageTypeEnum.fromValue(saveVO.getPackageType()).orElseThrow(() -> BizException.wrap("Invalid package type"));

        OtaPackageSignMethodEnum.fromValue(saveVO.getSignMethod()).orElseThrow(() -> BizException.wrap("Invalid sign method"));

        OtaPackageStatusEnum.fromValue(saveVO.getStatus()).orElseThrow(() -> BizException.wrap("Invalid status"));

        if (!VersionValidator.isValidVersion(saveVO.getVersion())) {
            throw BizException.wrap("无效版本号");
        }

        // 校验升级包版本号是否重复
        if (superManager.count(Wraps.<OtaUpgrades>lbQ().eq(OtaUpgrades::getPackageType, saveVO.getPackageType()).eq(OtaUpgrades::getVersion, saveVO.getVersion())) > 0) {
            throw BizException.wrap("升级版本号已存在");
        }
    }

    private OtaUpgrades buildOtaUpgradeFromSaveVO(OtaUpgradesSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, OtaUpgrades.class);
    }


    private void validateOtaUpgradesUpdateVO(OtaUpgradesUpdateVO updateVO) {

        OtaUpgrades existingOtaUpgrade = Optional.ofNullable(superManager.getById(updateVO.getId())).orElseThrow(() -> BizException.wrap("OTA upgrade package not found"));


        //TODO Validate the updateVO object
        String productIdentification = existingOtaUpgrade.getProductIdentification();

        OtaPackageTypeEnum.fromValue(updateVO.getPackageType()).orElseThrow(() -> BizException.wrap("Invalid package type"));

        OtaPackageSignMethodEnum.fromValue(updateVO.getSignMethod()).orElseThrow(() -> BizException.wrap("Invalid sign method"));

        OtaPackageStatusEnum.fromValue(updateVO.getStatus()).orElseThrow(() -> BizException.wrap("Invalid status"));


        // 校验升级包版本号是否合法
        if (!VersionValidator.isValidVersion(updateVO.getVersion())) {
            throw BizException.wrap("无效版本号");
        }

        // 校验升级包版本号是否重复
        if (superManager.count(Wraps.<OtaUpgrades>lbQ()
                .eq(OtaUpgrades::getPackageType, updateVO.getPackageType())
                .eq(OtaUpgrades::getVersion, updateVO.getVersion())
                .ne(OtaUpgrades::getId, updateVO.getId())) > 0) {
            throw BizException.wrap("升级版本号已存在");
        }
    }

    private Builder<OtaUpgrades> builderOtaUpgradesUpdateVO(OtaUpgradesUpdateVO updateVO) {
        return Builder.of(OtaUpgrades::new)
                .with(OtaUpgrades::setAppId, updateVO.getAppId())
                .with(OtaUpgrades::setPackageName, updateVO.getPackageName())
                .with(OtaUpgrades::setPackageType, updateVO.getPackageType())
                .with(OtaUpgrades::setProductIdentification, updateVO.getProductIdentification())
                .with(OtaUpgrades::setVersion, updateVO.getVersion())
                .with(OtaUpgrades::setProductVersionNo, updateVO.getProductVersionNo())
                .with(OtaUpgrades::setFileLocation, updateVO.getFileLocation())
                .with(OtaUpgrades::setSignMethod, updateVO.getSignMethod())
                .with(OtaUpgrades::setStatus, updateVO.getStatus())
                .with(OtaUpgrades::setDescription, updateVO.getDescription())
                .with(OtaUpgrades::setCustomInfo, updateVO.getCustomInfo())
                .with(OtaUpgrades::setRemark, updateVO.getRemark())
                .with(OtaUpgrades::setCreatedOrgId, ContextUtil.getCurrentDeptId());
    }

    /**
     * 根据ID集合查询升级包信息
     *
     * @param ids 升级包ID集合
     * @return {@link List<OtaUpgradesResultVO>} 升级包信息列表
     */
    @Override
    public List<OtaUpgradesResultVO> selectListByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<OtaUpgrades> upgrades = superManager.listByIds(ids);
        return Optional.ofNullable(upgrades)
                .map(upgradeList -> BeanPlusUtil.toBeanList(upgradeList, OtaUpgradesResultVO.class))
                .orElse(Collections.emptyList());
    }

    @Override
    public String resolveProductVersionNo(String productIdentification, String version, Integer packageType) {
        if (StrUtil.isBlank(productIdentification) || StrUtil.isBlank(version)) {
            return null;
        }
        // 同一(产品 + 版本)理论上唯一(saveUpgradePackage 已按 packageType + version 去重),取最新一条兜底多匹配
        return superManager.list(Wraps.<OtaUpgrades>lbQ()
                        .eq(OtaUpgrades::getProductIdentification, productIdentification)
                        .eq(OtaUpgrades::getVersion, version)
                        .eq(packageType != null, OtaUpgrades::getPackageType, packageType)
                        .isNotNull(OtaUpgrades::getProductVersionNo)
                        .ne(OtaUpgrades::getProductVersionNo, StrUtil.EMPTY)
                        .orderByDesc(OtaUpgrades::getId))
                .stream()
                .map(OtaUpgrades::getProductVersionNo)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
    }

}