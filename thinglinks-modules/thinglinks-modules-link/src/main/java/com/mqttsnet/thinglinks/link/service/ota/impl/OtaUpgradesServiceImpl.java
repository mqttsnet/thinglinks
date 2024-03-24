package com.mqttsnet.thinglinks.link.service.ota.impl;

import com.mqttsnet.thinglinks.common.core.exception.ArgumentException;
import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgrades;
import com.mqttsnet.thinglinks.link.api.domain.ota.enumeration.OtaPackageStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradesSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradesUpdateVO;
import com.mqttsnet.thinglinks.link.mapper.ota.OtaUpgradeTasksMapper;
import com.mqttsnet.thinglinks.link.mapper.ota.OtaUpgradesMapper;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradesService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
public class OtaUpgradesServiceImpl implements OtaUpgradesService {
    @Resource
    private OtaUpgradesMapper otaUpgradesMapper;
    @Resource
    private TokenService tokenService;

    @Resource
    private ProductService productService;

    @Resource
    private OtaUpgradeTasksMapper otaUpgradeTasksMapper;

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
        OtaUpgrades otaUpgrade = buildOtaUpgradesFromVO(saveVO);

        // Persist the OtaUpgrade entity using your manager or repository
        otaUpgradesMapper.insertOtaUpgrades(otaUpgrade);

        // Map the saved entity back to OtaUpgradesSaveVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesSaveVO.class);

    }

    @Override
    public OtaUpgradesUpdateVO updateUpgradePackage(OtaUpgradesUpdateVO updateVO) {
        log.info("Updating OTA upgrade package: {}", updateVO);

        // Validate the updateVO object
        validateOtaUpgradesUpdateVO(updateVO);

        // Map the saveVO to your OtaUpgrade entity
        OtaUpgrades otaUpgrade = buildOtaUpgradesFromVO(updateVO);

        // Save the updated entity
        otaUpgradesMapper.updateOtaUpgradeById(otaUpgrade);

        // Map the updated entity back to OtaUpgradesUpdateVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesUpdateVO.class);
    }

    @Override
    public Boolean updateOtaUpgradeStatus(Long id, Integer status) throws ArgumentException {
        ArgumentAssert.notNull(id, "Package ID cannot be null");
        ArgumentAssert.notNull(status, "Status cannot be null");

        OtaUpgrades otaUpgrades = otaUpgradesMapper.selectOtaUpgradeById(id);
        if (otaUpgrades == null) {
            throw new ServiceException("OTA upgrade package does not exist");
        }
        if (status == otaUpgrades.getStatus().intValue()) {
            throw new ServiceException("The OTA upgrade package status is the same as the current status");
        }
        return otaUpgradesMapper.updateOtaUpgradeByStatus(id, status) > 0;

    }

    /**
     * Deletes an OTA upgrade package by its ID if it is not in use.
     *
     * @param id the ID of the OTA upgrade package to delete
     * @return {@code true} if the package was successfully deleted, {@code false} otherwise
     * @throws IllegalArgumentException if the {@code id} is null
     * @throws ServiceException if the OTA upgrade package does not exist or is in use
     */
    @Override
    public Boolean deleteOtaUpgrade(Long id) throws ArgumentException {
        ArgumentAssert.notNull(id, "Package ID cannot be null");

        OtaUpgrades otaUpgrade = otaUpgradesMapper.selectOtaUpgradeById(id);
        if (otaUpgrade == null) {
            throw new ServiceException("OTA upgrade package does not exist");
        }

        int taskCount = otaUpgradeTasksMapper.getOtaUpgradeTasksByOtaUpgradeId(id);
        if (taskCount > 0) {
            throw new ServiceException("OTA upgrade package is in use and cannot be deleted");
        }

        boolean deleted = otaUpgradesMapper.deleteOtaUpgradeById(id) > 0;
        if (!deleted) {
            throw new ServiceException("Failed to delete OTA upgrade package");
        }

        return true;
    }



    private void validateOtaUpgradesUpdateVO(OtaUpgradesUpdateVO updateVO) {
        Optional.ofNullable(otaUpgradesMapper.selectOtaUpgradeById(updateVO.getId())).orElseThrow(() -> new ServiceException("OTA upgrade package not found"));
        String productIdentification = updateVO.getProductIdentification();
        Optional.ofNullable(productService.selectByProductIdentification(productIdentification)).orElseThrow(() -> new ServiceException("Product identification not found"));
        OtaPackageTypeEnum.fromValue(updateVO.getPackageType()).orElseThrow(() -> new ServiceException("Invalid package type"));
        OtaPackageStatusEnum.fromValue(updateVO.getStatus()).orElseThrow(() -> new ServiceException("Invalid status"));
    }

    private <T> OtaUpgrades buildOtaUpgradesFromVO(T vo) {
        SysUser sysUser = tokenService.getLoginUser().getSysUser();
        OtaUpgrades otaUpgrades = BeanPlusUtil.toBeanIgnoreError(vo, OtaUpgrades.class);
        otaUpgrades.setCreatedBy(sysUser.getUserName());
        otaUpgrades.setUpdatedBy(sysUser.getUserName());
        return otaUpgrades;
    }

    private void validateOtaUpgradesSaveVO(OtaUpgradesSaveVO saveVO) {
        OtaPackageTypeEnum.fromValue(saveVO.getPackageType()).orElseThrow(() -> new ServiceException("Invalid package type"));
        OtaPackageStatusEnum.fromValue(saveVO.getStatus()).orElseThrow(() -> new ServiceException("Invalid status"));
    }
}
