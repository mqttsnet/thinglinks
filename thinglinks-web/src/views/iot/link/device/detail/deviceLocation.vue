<template>
  <!--
    设备位置 Tab(Flexy)。

    设计意图:
    - 上下两段:位置概要卡片 + 操作区 + 地图,适配 tab 宽度
    - 概要卡片用 grid 网格展示 fullName / 经纬度 / 行政区划翻译 / 更新时间 + 人 / 备注
    - 操作区:主操作"更新位置"(主色,不再用 danger 错误暗示),辅助操作"复制坐标 / 在高德打开"
    - 空态:友好引导,避免孤零零按钮
  -->
  <div class="device-location">
    <!-- ============ 有位置数据:概要 + 操作 + 地图 ============ -->
    <template v-if="hasLocation">
      <!-- 位置概要卡片 -->
      <div class="loc-section">
        <div class="section-title">
          <span class="title-bar" />
          {{ t('iot.link.device.device.location.summary') }}
        </div>
        <div class="loc-grid">
          <div class="loc-item is-full">
            <div class="loc-label">
              <EnvironmentOutlined class="loc-icon" />
              {{ t('iot.link.device.device.location.address') }}
            </div>
            <div class="loc-value">
              <span class="text">{{ deviceLocationResultVO.fullName || '-' }}</span>
              <a-tooltip
                v-if="deviceLocationResultVO.fullName"
                :title="t('common.title.copy')"
              >
                <CopyOutlined class="action-icon" @click="copyText(deviceLocationResultVO.fullName)" />
              </a-tooltip>
            </div>
          </div>

          <div class="loc-item">
            <div class="loc-label">
              <AimOutlined class="loc-icon" />
              {{ t('iot.link.device.device.location.coordinate') }}
            </div>
            <div class="loc-value">
              <span class="text mono">{{ coordinateText }}</span>
              <a-tooltip :title="t('common.title.copy')">
                <CopyOutlined class="action-icon" @click="copyText(coordinateText)" />
              </a-tooltip>
            </div>
          </div>

          <div class="loc-item">
            <div class="loc-label">
              <CompassOutlined class="loc-icon" />
              {{ t('iot.link.device.device.location.region') }}
            </div>
            <div class="loc-value">
              <span class="text">{{ regionText }}</span>
            </div>
          </div>

          <div class="loc-item">
            <div class="loc-label">
              <ClockCircleOutlined class="loc-icon" />
              {{ t('iot.link.device.device.location.lastUpdate') }}
            </div>
            <div class="loc-value">
              <span class="text">{{ updateText }}</span>
            </div>
          </div>

          <div v-if="deviceLocationResultVO.remark" class="loc-item is-full">
            <div class="loc-label">
              <FileTextOutlined class="loc-icon" />
              {{ t('iot.link.device.device.remark') }}
            </div>
            <div class="loc-value">
              <span class="text">{{ deviceLocationResultVO.remark }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="loc-actions">
        <a-button type="primary" @click="handleEdit">
          <template #icon><EditOutlined /></template>
          {{ t('iot.link.device.device.updatePositionButton') }}
        </a-button>
        <a-button @click="copyText(coordinateText)">
          <template #icon><CopyOutlined /></template>
          {{ t('iot.link.device.device.location.copyCoordinate') }}
        </a-button>
        <a-button @click="openInAmap">
          <template #icon><GlobalOutlined /></template>
          {{ t('iot.link.device.device.location.openInAmap') }}
        </a-button>
        <a-tooltip :title="t('iot.link.device.device.location.recenterTip')">
          <a-button @click="recenterMap">
            <template #icon><AimOutlined /></template>
            {{ t('iot.link.device.device.location.recenter') }}
          </a-button>
        </a-tooltip>
      </div>

      <!-- 地图卡片 -->
      <div class="loc-section loc-map-section">
        <div class="section-title">
          <span class="title-bar map-bar" />
          {{ t('iot.link.device.device.location.mapPreview') }}
        </div>
        <div class="map-wrap">
          <!--
            :key 绑 mapReloadKey ── 用户在高德内点击其他位置后,marker 会跟着跑,
            没有"重置回设备坐标"的内置 API。最简实现:递增 key 强制 unmount + remount,
            新实例按 :value 重新渲染到设备原始坐标,等价于"一键回到设备位置"。
          -->
          <AMap
            :key="mapReloadKey"
            :value="[
              Number(deviceLocationResultVO.longitude),
              Number(deviceLocationResultVO.latitude),
            ]"
          />
        </div>
      </div>
    </template>

    <!-- ============ 无位置数据:空态引导 ============ -->
    <div v-else class="loc-empty">
      <div class="empty-icon">
        <EnvironmentOutlined />
      </div>
      <div class="empty-title">{{ t('iot.link.device.device.location.emptyTitle') }}</div>
      <div class="empty-desc">{{ t('iot.link.device.device.location.emptyDesc') }}</div>
      <a-button type="primary" size="large" @click="handleAdd">
        <template #icon><PlusOutlined /></template>
        {{ t('iot.link.device.device.location.addLocation') }}
      </a-button>
    </div>

    <EditModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts">
  import { defineComponent, computed, ref, PropType } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Button, Tooltip } from 'ant-design-vue';
  import {
    EditOutlined,
    EnvironmentOutlined,
    AimOutlined,
    CompassOutlined,
    ClockCircleOutlined,
    FileTextOutlined,
    CopyOutlined,
    GlobalOutlined,
    PlusOutlined,
  } from '@ant-design/icons-vue';
  import EditModal from '/@/views/iot/link/device/location/Edit.vue';
  import { useModal } from '/@/components/Modal';
  import { ActionEnum } from '/@/enums/commonEnum';
  import AMap from '/@/components/Form/src/components/AMap.vue';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common';
  import citiesGd from '/@/utils/thinglinks/citiesGd.json';

  /** 行政区划 cascader 节点(citiesGd.json 三级结构) */
  interface RegionNode {
    value: string;
    label: string;
    children?: RegionNode[];
  }

  /**
   * 三级行政区划编码 → 中文名翻译。
   *
   * <p>实现:在 citiesGd.json(省→市→区 cascader 数据)中按编码定位每一级 label。
   * 找不到某一级时直接 fallback 用编码本身,保证不出 undefined。</p>
   */
  function resolveRegion(
    provinceCode?: string,
    cityCode?: string,
    regionCode?: string,
  ): { province: string; city: string; region: string } {
    const data = citiesGd as RegionNode[];
    const empty = { province: '', city: '', region: '' };
    if (!provinceCode) return empty;

    const province = data.find((p) => p.value === provinceCode);
    if (!province) return { province: provinceCode, city: cityCode || '', region: regionCode || '' };

    const city = province.children?.find((c) => c.value === cityCode);
    if (!city) {
      return { province: province.label, city: cityCode || '', region: regionCode || '' };
    }

    const region = city.children?.find((r) => r.value === regionCode);
    return {
      province: province.label,
      city: city.label,
      region: region?.label || regionCode || '',
    };
  }

  export default defineComponent({
    name: 'DeviceLocation',
    components: {
      EditModal,
      AButton: Button,
      ATooltip: Tooltip,
      AMap,
      EditOutlined,
      EnvironmentOutlined,
      AimOutlined,
      CompassOutlined,
      ClockCircleOutlined,
      FileTextOutlined,
      CopyOutlined,
      GlobalOutlined,
      PlusOutlined,
    },
    emits: ['success'],
    props: {
      deviceIdentification: {
        type: String,
        default: '',
      },
      /**
       * 父组件传入的位置详情;props 直接用,无需 ref 中转 + onMounted load
       * (老实现 `let deviceLocationResultVO = ref({}) + load()` 是冗余的)
       */
      deviceLocationResultVO: {
        type: Object as PropType<any>,
        default: () => ({}),
      },
    },
    setup(props, { emit }) {
      const { t } = useI18n();
      const [registerModal, { openModal }] = useModal();

      /**
       * 地图重渲染标志位 ── 递增触发 <AMap :key="..."> 强制 unmount+remount,
       * 用于"回到设备位置"按钮:在高德 SDK 没有公开 reset API 的前提下,
       * 销毁旧实例重建新实例是最稳的恢复手段(也顺带清除用户随手缩放/拖拽产生的状态)。
       */
      const mapReloadKey = ref(0);
      function recenterMap() {
        mapReloadKey.value += 1;
      }

      /** 经纬度都存在才算"有位置";避免脏数据(只填了 latitude 漏 longitude)漏到地图崩 */
      const hasLocation = computed(() => {
        const v = props.deviceLocationResultVO;
        return v?.latitude != null && v?.longitude != null
          && v.latitude !== '' && v.longitude !== '';
      });

      /** "经度, 纬度" 文本(给 UI 展示 + 复制 + 高德深链),保留 6 位小数 */
      const coordinateText = computed(() => {
        const v = props.deviceLocationResultVO;
        if (!hasLocation.value) return '-';
        const lng = Number(v.longitude).toFixed(6);
        const lat = Number(v.latitude).toFixed(6);
        return `${lng}, ${lat}`;
      });

      /** "省 / 市 / 区" 翻译文本;任意一级缺失走 fallback 不报 undefined */
      const regionText = computed(() => {
        const v = props.deviceLocationResultVO;
        const r = resolveRegion(v?.provinceCode, v?.cityCode, v?.regionCode);
        const parts = [r.province, r.city, r.region].filter(Boolean);
        return parts.length ? parts.join(' / ') : '-';
      });

      /**
       * "更新时间 · 操作人"。
       *
       * <p>操作人只取 {@code echoMap}(后端 @Echo 回填的用户昵称)── 不 fallback
       * 到原始 updatedBy(Long ID),避免在 UI 上展示 `1452186486253289472`
       * 这种无意义雪花 ID。</p>
       *
       * <p>当前后端 {@code echoService.action()} 不递归嵌套 VO,
       * DeviceLocationResultVO 的 echoMap 实际是空的 → 这里会只显示纯时间。
       * 后续若后端补齐递归 echo,操作人会自动出现。</p>
       */
      const updateText = computed(() => {
        const v = props.deviceLocationResultVO;
        const time = v?.updatedTime || v?.createdTime || '-';
        const by = v?.echoMap?.updatedBy || v?.echoMap?.createdBy;
        return by ? `${time} · ${by}` : String(time);
      });

      /** 在高德地图深链中打开 ── 用户可在 PC 浏览器看实景街景 / 导航 */
      function openInAmap() {
        const v = props.deviceLocationResultVO;
        if (!hasLocation.value) return;
        const lng = encodeURIComponent(v.longitude);
        const lat = encodeURIComponent(v.latitude);
        const name = encodeURIComponent(v.fullName || t('iot.link.device.device.location.markerName'));
        // 高德地图 uri 协议(官方公开 API):https://lbs.amap.com/api/uri-api/guide/mobile-web/marker
        const url = `https://uri.amap.com/marker?position=${lng},${lat}&name=${name}&coordinate=gaode&callnative=0`;
        window.open(url, '_blank', 'noopener');
      }

      /**
       * 复制文本 ── handleCopyTextV2 内部已经弹 message.success,
       * 这里只 await 即可,不要再调 createMessage.success,否则会弹两个 toast。
       */
      async function copyText(text: string) {
        if (!text || text === '-') return;
        await handleCopyTextV2(text);
      }

      function handleAdd(e?: Event) {
        e?.stopPropagation();
        openModal(true, {
          deviceIdentification: props.deviceIdentification,
          type: ActionEnum.ADD,
        });
      }

      function handleEdit(e?: Event) {
        e?.stopPropagation();
        openModal(true, {
          record: props.deviceLocationResultVO,
          type: ActionEnum.EDIT,
        });
      }

      function handleSuccess(deviceLocationSaveVO: any) {
        emit('success', deviceLocationSaveVO);
      }

      return {
        t,
        registerModal,
        deviceLocationResultVO: computed(() => props.deviceLocationResultVO || {}),
        hasLocation,
        coordinateText,
        regionText,
        updateText,
        copyText,
        openInAmap,
        recenterMap,
        mapReloadKey,
        handleAdd,
        handleEdit,
        handleSuccess,
      };
    },
  });
</script>

<style lang="less" scoped>
  .device-location {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  /* ===== 公共卡片样式 ===== */
  .loc-section {
    background: #fff;
    border: 1px solid #eef1f7;
    border-radius: 12px;
    padding: 14px 18px 16px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #2a3547;
    margin-bottom: 14px;

    .title-bar {
      width: 3px;
      height: 14px;
      border-radius: 2px;
      background: #5d87ff;

      &.map-bar {
        background: #13c2c2;
      }
    }
  }

  /* ===== 概要 grid ===== */
  .loc-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px 28px;
  }

  .loc-item {
    min-width: 0;

    &.is-full {
      grid-column: 1 / -1;
    }
  }

  .loc-label {
    font-size: 12px;
    color: #8c97a5;
    margin-bottom: 6px;
    display: inline-flex;
    align-items: center;
    gap: 6px;

    .loc-icon {
      font-size: 12px;
      color: #5d87ff;
    }
  }

  .loc-value {
    font-size: 14px;
    color: #2a3547;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    line-height: 1.5;
    max-width: 100%;

    .text {
      word-break: break-all;
      font-weight: 500;

      &.mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 13px;
        color: #4a5568;
      }
    }

    .action-icon {
      color: #97a1b0;
      cursor: pointer;
      font-size: 14px;
      transition: color 0.18s ease;
      flex-shrink: 0;

      &:hover {
        color: #5d87ff;
      }
    }
  }

  /* ===== 操作区 ===== */
  .loc-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0 2px;
  }

  /* ===== 地图卡片 ===== */
  .loc-map-section {
    padding-bottom: 18px;
  }

  .map-wrap {
    width: 100%;
    height: 480px;
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid #eef1f7;
    background: #fafbfd;

    /* AMap 组件根 div 撑满 */
    :deep(> div),
    :deep(.amap-container),
    :deep(.amap-wrap) {
      width: 100%;
      height: 100%;
    }
  }

  /* ===== 空态 ===== */
  .loc-empty {
    background: linear-gradient(180deg, #fafbfd 0%, #ffffff 100%);
    border: 1px dashed #d6dce8;
    border-radius: 14px;
    padding: 60px 24px;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .empty-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: linear-gradient(135deg, #eef3ff 0%, #e6efff 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 38px;
      color: #5d87ff;
      box-shadow: 0 8px 24px rgba(93, 135, 255, 0.18);
      margin-bottom: 4px;
    }

    .empty-title {
      font-size: 16px;
      font-weight: 600;
      color: #2a3547;
    }

    .empty-desc {
      font-size: 13px;
      color: #97a1b0;
      max-width: 360px;
      line-height: 1.6;
      margin-bottom: 8px;
    }
  }
</style>
