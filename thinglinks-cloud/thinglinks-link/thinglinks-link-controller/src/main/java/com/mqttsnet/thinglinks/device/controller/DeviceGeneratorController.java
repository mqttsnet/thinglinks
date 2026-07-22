package com.mqttsnet.thinglinks.device.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.log.StaticLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.entity.DeviceLocation;
import com.mqttsnet.thinglinks.device.service.DeviceLocationService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备数据生成控制器
 * 功能：支持生成大量设备测试数据，包含随机位置信息
 *
 * @author mqttsnet
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/device/generator")
@Tag(name = "设备数据生成器")
public class DeviceGeneratorController {

    // 预定义的中国主要城市位置信息（基础坐标）
    private static final List<DeviceLocation> BASE_LOCATIONS = new ArrayList<>();

    static {
        // 初始化预定义位置信息（30个中国主要城市）
        // 格式：城市名称, 省编码, 市编码, 区编码, 经度, 纬度
        addBaseLocation("北京市", "110000", "110100", "110101", 116.4074, 39.9042);
        addBaseLocation("上海市", "310000", "310100", "310101", 121.4737, 31.2304);
        addBaseLocation("广州市", "440000", "440100", "440103", 113.2644, 23.1291);
        addBaseLocation("深圳市", "440000", "440300", "440303", 114.0579, 22.5431);
        addBaseLocation("杭州市", "330000", "330100", "330102", 120.1551, 30.2741);
        addBaseLocation("成都市", "510000", "510100", "510104", 104.0665, 30.5728);
        addBaseLocation("武汉市", "420000", "420100", "420102", 114.2986, 30.5844);
        addBaseLocation("南京市", "320000", "320100", "320102", 118.7969, 32.0603);
        addBaseLocation("西安市", "610000", "610100", "610102", 108.9402, 34.3416);
        addBaseLocation("重庆市", "500000", "500100", "500101", 106.5516, 29.5630);
        addBaseLocation("天津市", "120000", "120100", "120101", 117.1994, 39.0851);
        addBaseLocation("苏州市", "320000", "320500", "320505", 120.5853, 31.2990);
        addBaseLocation("厦门市", "350000", "350200", "350203", 118.0894, 24.4798);
        addBaseLocation("长沙市", "430000", "430100", "430102", 112.9388, 28.2282);
        addBaseLocation("青岛市", "370000", "370200", "370202", 120.3826, 36.0671);
        addBaseLocation("大连市", "210000", "210200", "210202", 121.6147, 38.9140);
        addBaseLocation("宁波市", "330000", "330200", "330203", 121.5497, 29.8683);
        addBaseLocation("济南市", "370000", "370100", "370102", 117.1201, 36.6510);
        addBaseLocation("沈阳市", "210000", "210100", "210102", 123.4315, 41.8057);
        addBaseLocation("合肥市", "340000", "340100", "340102", 117.2272, 31.8206);
        addBaseLocation("郑州市", "410000", "410100", "410102", 113.6254, 34.7466);
        addBaseLocation("福州市", "350000", "350100", "350102", 119.2965, 26.0745);
        addBaseLocation("哈尔滨市", "230000", "230100", "230102", 126.5350, 45.8038);
        addBaseLocation("昆明市", "530000", "530100", "530102", 102.8329, 24.8801);
        addBaseLocation("长春市", "220000", "220100", "220102", 125.3235, 43.8171);
        addBaseLocation("南昌市", "360000", "360100", "360102", 115.8581, 28.6832);
        addBaseLocation("南宁市", "450000", "450100", "450102", 108.3200, 22.8240);
        addBaseLocation("贵阳市", "520000", "520100", "520102", 106.6302, 26.6470);
        addBaseLocation("太原市", "140000", "140100", "140105", 112.5492, 37.8570);
        addBaseLocation("兰州市", "620000", "620100", "620102", 103.8343, 36.0610);
    }

    @Resource
    private DeviceService deviceService;
    @Resource
    private DeviceLocationService deviceLocationService;
    @Resource
    private ProductService productService;

    /**
     * 添加基础位置信息的辅助方法
     */
    private static void addBaseLocation(String fullName, String provinceCode, String cityCode,
                                        String regionCode, double longitude, double latitude) {
        BASE_LOCATIONS.add(DeviceLocation.builder()
                .fullName(fullName)
                .provinceCode(provinceCode)
                .cityCode(cityCode)
                .regionCode(regionCode)
                .longitude(BigDecimal.valueOf(longitude))
                .latitude(BigDecimal.valueOf(latitude))
                .build());
    }

    /**
     * 生成设备数据接口
     */
    @Operation(summary = "生成普通设备数据", description = "批量生成普通设备测试数据，包含随机位置信息")
    @PostMapping("/generate")
    public R<String> generateDevices(
            @Parameter(description = "租户ID", required = true) @RequestParam Long tenantId,
            @Parameter(description = "产品标识", required = true) @RequestParam String productId,
            @Parameter(description = "总设备数", example = "10000") @RequestParam(defaultValue = "10000") int totalDevices,
            @Parameter(description = "每批数量", example = "500") @RequestParam(defaultValue = "500") int batchSize,
            @Parameter(description = "起始序号", example = "1") @RequestParam(defaultValue = "1") int startNumber,
            @Parameter(description = "设备用户名", example = "123456") @RequestParam(defaultValue = "123456") String username,
            @Parameter(description = "设备密码", example = "123456") @RequestParam(defaultValue = "123456") String password) {

        // 参数校验
        validateParameters(totalDevices, batchSize, startNumber);

        // 设置租户上下文
        ContextUtil.setTenantId(tenantId);

        ProductResultVO productResultVO = productService.findOneByProductIdentification(productId);
        ArgumentAssert.notNull(productResultVO, "产品不存在");

        try {
            TimeInterval timer = DateUtil.timer();
            AtomicInteger counter = new AtomicInteger(0);

            // 计算总批次数
            int totalBatches = (totalDevices + batchSize - 1) / batchSize;

            // 分批次处理
            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                // 计算当前批次实际大小（最后一批可能不满）
                int currentBatchSize = Math.min(batchSize, totalDevices - batchIndex * batchSize);
                int currentStart = startNumber + batchIndex * batchSize;

                // 生成设备批次
                List<Device> devices = new ArrayList<>(currentBatchSize);
                List<DeviceLocation> locations = new ArrayList<>(currentBatchSize);

                for (int i = 0; i < currentBatchSize; i++) {
                    int deviceNumber = currentStart + i;
                    Device device = generateSingleDevice(deviceNumber, productResultVO, username, password);
                    devices.add(device);

                    // 生成位置信息
                    DeviceLocation location = generateRandomLocation(
                            buildDeviceIdentifier(productResultVO, deviceNumber)
                    );
                    locations.add(location);

                    // 更新进度
                    int currentCount = counter.incrementAndGet();
                    if (currentCount % 1000 == 0) {
                        log.info("已生成设备: {}/{} ({}%)", currentCount, totalDevices, String.format("%.2f", (currentCount * 100.0 / totalDevices)));
                    }
                }

                // 批量保存
                saveBatchData(devices, locations, batchIndex);
            }

            String msg = String.format("成功生成 %d 条设备数据(从%d开始)，总耗时: %d 毫秒",
                    totalDevices, startNumber, timer.interval());
            StaticLog.info(msg);
            return R.success(msg);
        } catch (Exception e) {
            log.error("生成设备数据失败", e);
            return R.fail("生成设备数据失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存设备数据
     */
    private void saveBatchData(List<Device> devices, List<DeviceLocation> locations, int batchIndex) {
        try {
            if (!devices.isEmpty()) {
                deviceService.saveBatch(devices);
                deviceLocationService.saveBatch(locations);
                log.info("批次 {} 完成，保存 {} 条设备", batchIndex, devices.size());
            }
        } catch (Exception e) {
            log.error("批次 {} 保存失败: {}", batchIndex, e.getMessage());
        }
    }

    /**
     * 参数校验
     */
    private void validateParameters(int totalDevices, int batchSize, int startNumber) {
        ArgumentAssert.isFalse(totalDevices <= 0, "设备数量必须大于0");
        ArgumentAssert.isFalse(batchSize <= 0, "批次大小必须大于0");
        ArgumentAssert.isFalse(startNumber < 1, "起始序号必须大于等于1");
        ArgumentAssert.isFalse(batchSize > 1000, "批次大小不能超过1000");
    }


    /**
     * 生成单个设备数据
     */
    private Device generateSingleDevice(int deviceNumber, ProductResultVO productResultVO,
                                        String username, String password) {
        // 构建设备基本信息
        return Device.builder()
                .appId("thinglinks-test")
                .deviceName(buildDeviceName(productResultVO, deviceNumber))
                .deviceIdentification(buildDeviceIdentifier(productResultVO, deviceNumber))
                .clientId(buildClientId(productResultVO, deviceNumber, ContextUtil.getTenantId()))
                .userName(username)
                .password(password)
                .authMode(0)
                .deviceStatus(1)
                .connectStatus(0)
                .productIdentification(productResultVO.getProductIdentification())
                .nodeType(0)
                .encryptMethod(0)
                .deviceSdkVersion("v1")
                .fwVersion("v1.0.0")
                .swVersion("v1.0.0")
                .remark(productResultVO.getProductName() + "批量生成设备序号-" + deviceNumber)
                .build();
    }

    /**
     * 生成随机位置信息（基础位置+随机偏移）
     */
    private DeviceLocation generateRandomLocation(String deviceIdentification) {
        // 随机选择一个基础位置
        DeviceLocation baseLocation = RandomUtil.randomEle(BASE_LOCATIONS);

        // 生成随机偏移量（-0.8到+0.8度之间）
        double longitudeOffset = RandomUtil.randomDouble(-0.8, 0.8);
        double latitudeOffset = RandomUtil.randomDouble(-0.8, 0.8);

        // 计算最终坐标（保留6位小数）
        BigDecimal longitude = baseLocation.getLongitude()
                .add(BigDecimal.valueOf(longitudeOffset))
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal latitude = baseLocation.getLatitude()
                .add(BigDecimal.valueOf(latitudeOffset))
                .setScale(6, RoundingMode.HALF_UP);

        // 构建最终位置信息
        return baseLocation.toBuilder()
                .deviceIdentification(deviceIdentification)
                .longitude(longitude)
                .latitude(latitude)
                .remark("随机位置-" + deviceIdentification + " 偏移:(" + longitudeOffset + "," + latitudeOffset + ")")
                .build();
    }


    /**
     * 构建设备标识信息
     */
    private String buildDeviceIdentifier(ProductResultVO productResultVO, int deviceNumber) {
        return String.format("%s_%d", productResultVO.getProductIdentification(), deviceNumber);
    }

    /**
     * 构建设备名称
     */
    private String buildDeviceName(ProductResultVO productResultVO, int deviceNumber) {
        return String.format("%s测试设备-%s_%d", productResultVO.getProductName(), productResultVO.getProductIdentification(), deviceNumber);
    }

    /**
     * 构建客户端ID
     */
    private String buildClientId(ProductResultVO productResultVO, int deviceNumber, Long tenantId) {
        return TenantUtil.buildOptionalItem(
                buildDeviceIdentifier(productResultVO, deviceNumber),
                tenantId.toString()
        );
    }
}
