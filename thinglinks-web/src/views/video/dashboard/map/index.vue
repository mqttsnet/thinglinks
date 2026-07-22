<template>
  <div class="map-wrap">
    <div ref="mapRef" class="map-container" />

    <!-- 右侧面板容器 -->
    <div class="panel-container">
      <!-- 搜索面板 -->
      <div class="search-panel" :class="{ collapsed: panelCollapsed }">
        <div v-if="panelCollapsed" class="collapsed-btns">
          <div class="panel-toggle-btn" @click="panelCollapsed = false">
            <SearchOutlined />
          </div>
        </div>
        <template v-else>
          <div class="panel-header">
            <span class="panel-title">
              {{ t('video.dashboard.map.panelTitle') }}
              <span v-if="filteredChannels.length" class="panel-badge">{{ filteredChannels.length }}</span>
            </span>
            <span class="panel-header-actions">
              <span class="panel-collapse-btn" @click="panelCollapsed = true">
                <MenuFoldOutlined />
              </span>
            </span>
          </div>
          <div class="panel-search">
            <div class="search-input-group">
              <Select
                v-model:value="searchField"
                class="search-field-select"
                :dropdown-match-select-width="false"
                :options="searchFieldOptions"
                @change="handleSearch"
              />
              <Input
                v-model:value="searchText"
                class="search-keyword-input"
                :placeholder="searchPlaceholder"
                allow-clear
                @input="debouncedSearch"
                @change="onSearchChange"
              >
                <template #suffix>
                  <SearchOutlined style="color: #bfbfbf; font-size: 13px" />
                </template>
              </Input>
            </div>
          </div>
          <!-- 状态筛选 -->
          <div class="panel-filter">
            <a-radio-group v-model:value="statusFilter" size="small" button-style="solid" @change="handleSearch">
              <a-radio-button :value="BizConstant.ALL">{{ t('video.dashboard.map.filterAll') }}</a-radio-button>
              <a-radio-button value="online">{{ t('video.dashboard.map.online') }}</a-radio-button>
              <a-radio-button value="offline">{{ t('video.dashboard.map.offline') }}</a-radio-button>
              <a-radio-button value="alarm">{{ t('video.dashboard.map.filterAlarm') }}</a-radio-button>
            </a-radio-group>
          </div>
          <div class="panel-results">
            <div v-if="dataLoading" class="panel-loading">
              <LoadingOutlined spin />
              <span>{{ t('video.dashboard.map.loading') }}</span>
            </div>
            <div v-else-if="filteredChannels.length > 0" class="result-list">
              <div
                v-for="ch in filteredChannels"
                :key="ch.id"
                class="result-item"
                @click="locateChannel(ch)"
              >
                <span
                  class="status-dot"
                  :class="{
                    online: ch.onlineStatus,
                    offline: !ch.onlineStatus,
                    alarm: ch.unhandledAlarmCount > 0,
                  }"
                />
                <div class="result-info">
                  <div class="result-name">{{ ch.channelName || ch.channelIdentification }}</div>
                  <div v-if="ch.deviceIdentification" class="result-id">{{ ch.deviceIdentification }}</div>
                </div>
                <a-badge
                  v-if="ch.unhandledAlarmCount > 0"
                  :count="ch.unhandledAlarmCount"
                  :overflow-count="99"
                  :number-style="{ fontSize: '10px' }"
                />
              </div>
            </div>
            <div v-else-if="!dataLoading && channels.length > 0" class="panel-empty">
              {{ t('video.dashboard.map.noMatch') }}
            </div>
            <div v-else-if="!dataLoading && channels.length === 0" class="panel-hint">
              {{ t('video.dashboard.map.noData') }}
            </div>
          </div>
        </template>
      </div>

      <!-- 2D/3D 切换按钮 -->
      <div class="view-mode-btn" :class="{ active: is3D }" @click="toggleViewMode">
        {{ is3D ? '2D' : '3D' }}
      </div>
    </div>

    <!-- 加载进度 -->
    <div v-if="dataLoading" class="loading-indicator">
      <LoadingOutlined spin />
      <span>{{ t('video.dashboard.map.loadProgress') }} {{ loadProgress }}...</span>
    </div>

    <!-- 通道弹窗 -->
    <transition name="popup-fade">
      <div
        v-if="popup.visible"
        class="device-popup"
        :style="{ left: popup.x + 'px', top: popup.y + 'px' }"
      >
        <div class="popup-arrow" />
        <div class="popup-close" @click="closePopup">&times;</div>
        <div class="popup-header">
          <span class="popup-device-name">{{ popup.name }}</span>
          <span class="popup-status-tag" :class="popup.online ? 'tag-active' : 'tag-danger'">
            {{ popup.online ? t('video.dashboard.map.online') : t('video.dashboard.map.offline') }}
          </span>
        </div>
        <div class="popup-body">
          <div class="popup-row">
            <span class="popup-label">{{ t('video.dashboard.map.deviceIdentification') }}</span>
            <span class="popup-value mono">{{ popup.deviceId }}</span>
          </div>
          <div class="popup-row">
            <span class="popup-label">{{ t('video.dashboard.map.channelIdentification') }}</span>
            <span class="popup-value mono">{{ popup.channelId }}</span>
          </div>
          <div class="popup-row">
            <span class="popup-label">{{ t('video.dashboard.map.coordinates') }}</span>
            <span class="popup-value">{{ popup.lng?.toFixed(6) }}, {{ popup.lat?.toFixed(6) }}</span>
          </div>
          <div v-if="popup.alarmCount > 0" class="popup-row">
            <span class="popup-label">{{ t('video.dashboard.map.unhandledAlarm') }}</span>
            <span class="popup-value" style="color: #ff4d4f; font-weight: 500">{{ popup.alarmCount }}</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
  import { ref, computed, onMounted, onBeforeUnmount, reactive, nextTick } from 'vue';
  import AMapLoader from '@amap/amap-jsapi-loader';
  import { useDebounceFn } from '@vueuse/core';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import { BizConstant } from '/@/enums/biz/common';
  import { useDict } from '/@/components/Dict';
  import { Input, Select } from 'ant-design-vue';
  import { SearchOutlined, LoadingOutlined, MenuFoldOutlined } from '@ant-design/icons-vue';
  import { getChannelLocationList } from '/@/api/video/dashboard/dashboard';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';

  const { t } = useI18n();
  const { initGetDictList } = useDict();

  // =====================================================================
  //  Canvas 生成水滴标记（无外部图片依赖）
  // =====================================================================
  function createMarkerIcon(color, w, h) {
    const canvas = document.createElement('canvas');
    const dpr = window.devicePixelRatio || 1;
    const cw = w * dpr;
    const ch = h * dpr;
    canvas.width = cw;
    canvas.height = ch;
    const ctx = canvas.getContext('2d');
    const cx = cw / 2;
    const r = cw * 0.42;
    const circleY = r + dpr * 0.5;
    const tipY = ch - dpr * 0.5;

    ctx.beginPath();
    ctx.moveTo(cx, tipY);
    ctx.bezierCurveTo(cx + r * 0.4, circleY + r * 0.95, cx + r, circleY + r * 0.4, cx + r, circleY);
    ctx.arc(cx, circleY, r, 0, Math.PI, true);
    ctx.bezierCurveTo(cx - r, circleY + r * 0.4, cx - r * 0.4, circleY + r * 0.95, cx, tipY);
    ctx.closePath();
    ctx.fillStyle = color;
    ctx.fill();

    return canvas.toDataURL('image/png');
  }

  // =====================================================================
  //  状态
  // =====================================================================
  const mapRef = ref(null);
  let AMap = null;
  let map = null;
  let massMarks = null;
  let isUnmounted = false;

  const searchText = ref('');
  const searchField = ref('channelName');
  const statusFilter = ref(BizConstant.ALL);
  const channels = ref([]);
  const dataLoading = ref(false);
  const panelCollapsed = ref(false);
  const is3D = ref(false);
  const loadedCount = ref(0);
  const totalCount = ref(0);

  const loadProgress = computed(() =>
    totalCount.value ? `${loadedCount.value}/${totalCount.value}` : '',
  );

  const searchFieldOptions = computed(() => [
    { value: 'channelName', label: t('video.dashboard.map.channelName') },
    { value: 'channelIdentification', label: t('video.dashboard.map.channelIdentification') },
    { value: 'deviceIdentification', label: t('video.dashboard.map.deviceIdentification') },
  ]);

  const searchPlaceholder = computed(() => {
    const opt = searchFieldOptions.value.find((o) => o.value === searchField.value);
    return `${t('video.dashboard.map.searchPrefix')}${opt?.label || ''}`;
  });

  const filteredChannels = computed(() => {
    let list = channels.value;
    if (searchText.value) {
      const kw = searchText.value.toLowerCase();
      list = list.filter(
        (ch) =>
          (ch.channelName || '').toLowerCase().includes(kw) ||
          (ch.channelIdentification || '').toLowerCase().includes(kw) ||
          (ch.deviceIdentification || '').toLowerCase().includes(kw),
      );
    }
    if (statusFilter.value === 'online') {
      list = list.filter((ch) => ch.onlineStatus);
    } else if (statusFilter.value === 'offline') {
      list = list.filter((ch) => !ch.onlineStatus);
    } else if (statusFilter.value === 'alarm') {
      list = list.filter((ch) => ch.unhandledAlarmCount > 0);
    }
    return list;
  });

  // =====================================================================
  //  搜索
  // =====================================================================
  const handleSearch = () => {
    renderMarkers();
    const first = filteredChannels.value[0];
    if (first?.longitude && first?.latitude) {
      locateChannel(first);
    }
  };

  const debouncedSearch = useDebounceFn(() => handleSearch(), 400);

  const onSearchChange = (e) => {
    if (!e.target.value) {
      renderMarkers();
    }
  };

  // =====================================================================
  //  弹窗
  // =====================================================================
  const MARKER_W = 14;
  const MARKER_H = 20;

  const popup = reactive({
    visible: false,
    x: 0,
    y: 0,
    lng: null,
    lat: null,
    name: '',
    deviceId: '',
    channelId: '',
    online: false,
    alarmCount: 0,
  });

  function closePopup() {
    popup.visible = false;
  }

  function updatePopupPixel() {
    if (!map || popup.lng == null) return;
    const pixel = map.lngLatToContainer(new AMap.LngLat(popup.lng, popup.lat));
    popup.x = Math.round(pixel.getX());
    popup.y = Math.round(pixel.getY()) - MARKER_H - 4;
  }

  function showPopupForChannel(ch) {
    popup.lng = ch.longitude;
    popup.lat = ch.latitude;
    popup.name = ch.channelName || ch.channelIdentification;
    popup.deviceId = ch.deviceIdentification || '-';
    popup.channelId = ch.channelIdentification || '-';
    popup.online = !!ch.onlineStatus;
    popup.alarmCount = ch.unhandledAlarmCount || 0;
    updatePopupPixel();
    popup.visible = true;
  }

  function locateChannel(ch) {
    if (map && ch.longitude && ch.latitude) {
      map.setZoomAndCenter(16, [ch.longitude, ch.latitude], false, 600);
      showPopupForChannel(ch);
    }
  }

  // =====================================================================
  //  2D/3D
  // =====================================================================
  function toggleViewMode() {
    if (!map) return;
    is3D.value = !is3D.value;
    map.setPitch(is3D.value ? 45 : 0, true);
  }

  // =====================================================================
  //  地图初始化
  // =====================================================================
  async function initMap(key) {
    AMap = await AMapLoader.load({
      key,
      version: '2.0',
      plugins: ['AMap.ToolBar', 'AMap.MassMarks'],
    });

    map = new AMap.Map(mapRef.value, {
      viewMode: '3D',
      pitch: 0,
      zoom: 5,
      center: [104.5, 35.5],
      resizeEnable: true,
      mapStyle: 'amap://styles/normal',
      features: ['bg', 'road', 'building', 'point'],
    });

    map.addControl(new AMap.ToolBar({ position: { bottom: '40px', right: '20px' } }));

    // Canvas 生成水滴图标
    const onlineIcon = createMarkerIcon('#1890ff', MARKER_W, MARKER_H);
    const offlineIcon = createMarkerIcon('#c0c4cc', MARKER_W, MARKER_H);
    const alarmIcon = createMarkerIcon('#ff4d4f', MARKER_W, MARKER_H);

    massMarks = new AMap.MassMarks([], {
      zIndex: 111,
      zooms: [3, 20],
      cursor: 'pointer',
      style: [
        { url: onlineIcon, size: new AMap.Size(MARKER_W, MARKER_H), anchor: new AMap.Pixel(MARKER_W / 2, MARKER_H) },
        { url: offlineIcon, size: new AMap.Size(MARKER_W, MARKER_H), anchor: new AMap.Pixel(MARKER_W / 2, MARKER_H) },
        { url: alarmIcon, size: new AMap.Size(MARKER_W + 4, MARKER_H + 6), anchor: new AMap.Pixel((MARKER_W + 4) / 2, MARKER_H + 6) },
      ],
    });

    massMarks.on('click', (e) => {
      const d = e.data;
      const [lng, lat] = d.lnglat;
      const currentZoom = map.getZoom();
      if (currentZoom < 14) {
        map.setZoomAndCenter(14, [lng, lat], false, 400);
      } else {
        map.setCenter([lng, lat], false, 400);
      }
      // 从缓存找到通道信息
      const ch = channelMap.get(d.cid);
      if (ch) {
        showPopupForChannel(ch);
      }
    });

    massMarks.setMap(map);

    map.on('click', () => closePopup());
    map.on('mapmove', updatePopupPixel);
    map.on('zoomchange', updatePopupPixel);
    map.on('resize', updatePopupPixel);
  }

  // =====================================================================
  //  数据加载
  // =====================================================================
  const channelMap = new Map(); // channelIdentification → channel data

  async function loadChannels() {
    dataLoading.value = true;
    let page = 1;
    const size = 5000;
    const allChannels = [];

    try {
      while (!isUnmounted) {
        const res = await getChannelLocationList({ model: {}, size, current: page });
        const records = res?.records || [];
        totalCount.value = parseInt(res?.total) || 0;

        for (const ch of records) {
          // 解析经纬度（允许为空，无坐标的通道仍显示在列表中）
          if (ch.longitude && ch.latitude) {
            ch.longitude = Number(ch.longitude);
            ch.latitude = Number(ch.latitude);
            if (isNaN(ch.longitude) || isNaN(ch.latitude)) {
              ch.longitude = null;
              ch.latitude = null;
            }
          }
          allChannels.push(ch);
          channelMap.set(ch.channelIdentification, ch);
        }

        loadedCount.value = allChannels.length;

        // 首页立即渲染
        if (page === 1 && massMarks) {
          massMarks.setData(buildMassData(allChannels));
        }

        if (allChannels.length >= totalCount.value || records.length < size) break;
        page++;
      }

      channels.value = allChannels;
      renderMarkers();
    } catch (err) {
      console.error(t('video.dashboard.map.loadFailed'), err);
    } finally {
      dataLoading.value = false;
    }
  }

  function buildMassData(list) {
    return list
      .filter((ch) => ch.longitude && ch.latitude)
      .map((ch) => ({
        lnglat: [ch.longitude, ch.latitude],
        cid: ch.channelIdentification,
        style: ch.unhandledAlarmCount > 0 ? 2 : isTruthyStatus(ch.onlineStatus) ? 0 : 1,
      }));
  }

  function renderMarkers() {
    if (!massMarks || !channels.value.length) return;
    massMarks.setData(buildMassData(channels.value));
  }

  // =====================================================================
  //  生命周期
  // =====================================================================
  onMounted(async () => {
    const dictList = await initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_JS_API_KEY);
    const key = dictList.find((item) => item.key === 'key')?.name ?? '';
    if (!key) {
      console.error(t('video.dashboard.map.amapKeyMissing'));
      return;
    }
    await initMap(key);
    await nextTick();
    map?.resize();
    loadChannels();
  });

  onBeforeUnmount(() => {
    isUnmounted = true;
    massMarks?.setMap(null);
    map?.destroy();
    channelMap.clear();
  });
</script>

<style lang="less" scoped>
  @keyframes sk-pulse {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.4;
    }
  }

  .map-wrap {
    position: relative;
    overflow: hidden;
    background: #f0f2f5;
    width: 100%;
    height: 100%;
  }

  .map-container {
    width: 100%;
    height: 100%;
  }

  /* ========== 加载指示器 ========== */
  .loading-indicator {
    position: absolute;
    z-index: 10;
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 20px;
    background: #fff;
    border-radius: 20px;
    color: @primary-color;
    font-size: 13px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  /* ========== 面板容器 ========== */
  .panel-container {
    position: absolute;
    z-index: 10;
    top: 20px;
    right: 20px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 10px;
  }

  /* ========== 搜索面板 ========== */
  .search-panel {
    width: 340px;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;

    &.collapsed {
      width: auto;
      background: transparent;
      box-shadow: none;
    }
  }

  .collapsed-btns {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .panel-toggle-btn {
    width: 40px;
    height: 40px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 18px;
    color: #666;
    transition: all 0.2s;

    &:hover {
      color: @primary-color;
      box-shadow: 0 2px 16px rgba(0, 0, 0, 0.12);
    }
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 16px 8px;
  }

  .panel-header-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .panel-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .panel-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    background: @primary-color;
    color: #fff;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 700;
  }

  .panel-collapse-btn {
    cursor: pointer;
    color: #bfbfbf;
    font-size: 16px;
    transition: color 0.2s;

    &:hover {
      color: @primary-color;
    }
  }

  .panel-search {
    padding: 0 12px 8px;
  }

  .panel-filter {
    padding: 0 12px 10px;
    display: flex;
    justify-content: center;
  }

  // 搜索框整体容器
  .search-input-group {
    display: flex;
    align-items: stretch;
    border: 1px solid #e8e8e8;
    border-radius: 8px;
    overflow: hidden;
    transition: border-color 0.2s, box-shadow 0.2s;

    &:focus-within {
      border-color: @primary-color;
      box-shadow: 0 0 0 2px fade(@primary-color, 10%);
    }

    .search-field-select {
      flex-shrink: 0;
      width: 100px;
      text-align: center;

      :deep(.ant-select-selector) {
        border: none !important;
        border-right: 1px solid #e8e8e8 !important;
        border-radius: 0 !important;
        background: #fafafa !important;
        box-shadow: none !important;
        padding: 0 8px 0 10px !important;
        height: 32px !important;
      }

      :deep(.ant-select-selection-item) {
        font-size: 12px;
        color: #555;
        line-height: 32px !important;
      }

      :deep(.ant-select-arrow) {
        color: #bbb;
        font-size: 10px;
        right: 8px;
      }

      &:deep(.ant-select-focused .ant-select-selector) {
        box-shadow: none !important;
      }
    }

    .search-keyword-input {
      flex: 1;
      min-width: 0;

      :deep(.ant-input-affix-wrapper) {
        border: none !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        padding: 0 8px !important;
        height: 32px !important;

        &-focused {
          box-shadow: none !important;
        }
      }

      :deep(.ant-input) {
        font-size: 13px;
      }
    }
  }

  .panel-results {
    max-height: 50vh;
    overflow-y: auto;
    border-top: 1px solid #f0f0f0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #d9d9d9;
      border-radius: 2px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  .panel-loading,
  .panel-empty,
  .panel-hint {
    padding: 24px 14px;
    text-align: center;
    color: #bfbfbf;
    font-size: 13px;
  }

  .panel-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: @primary-color;
  }

  .result-list {
    padding: 4px 0;
  }

  .result-item {
    display: flex;
    align-items: center;
    padding: 9px 16px;
    cursor: pointer;
    transition: background-color 0.15s;

    &:hover {
      background: #f5f7fa;
    }

    &:active {
      background: #e8f0fe;
    }
  }

  .status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
    margin-right: 10px;

    &.online {
      background-color: #43cf7c;
      box-shadow: 0 0 4px rgba(67, 207, 124, 0.4);
    }

    &.offline {
      background-color: #c0c4cc;
    }

    &.alarm {
      background-color: #fa3758;
      box-shadow: 0 0 4px rgba(250, 55, 88, 0.4);
    }
  }

  .result-info {
    min-width: 0;
    flex: 1;
  }

  .result-name {
    font-size: 13px;
    font-weight: 500;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .result-id {
    font-size: 11px;
    color: #999;
    margin-top: 2px;
    font-family: 'SFMono-Regular', Consolas, monospace;
  }

  /* ========== 2D/3D 切换 ========== */
  .view-mode-btn {
    align-self: flex-end;
    width: 40px;
    height: 40px;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 13px;
    font-weight: 700;
    color: #666;
    letter-spacing: 0.5px;
    transition: all 0.2s;
    user-select: none;

    &:hover {
      color: @primary-color;
      box-shadow: 0 2px 16px rgba(0, 0, 0, 0.12);
    }

    &.active {
      background: @primary-color;
      color: #fff;
      box-shadow: 0 2px 12px fade(@primary-color, 30%);
    }
  }

  /* ========== 通道弹窗 ========== */
  .device-popup {
    position: absolute;
    z-index: 20;
    transform: translate(-50%, -100%);
    background: #fff;
    border-radius: 12px;
    padding: 14px 16px;
    min-width: 260px;
    max-width: 320px;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
    pointer-events: auto;
  }

  .popup-arrow {
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%) rotate(45deg);
    width: 12px;
    height: 12px;
    background: #fff;
    box-shadow: 3px 3px 6px rgba(0, 0, 0, 0.06);
  }

  .popup-close {
    position: absolute;
    top: 8px;
    right: 12px;
    cursor: pointer;
    font-size: 18px;
    color: #ccc;
    line-height: 1;
    transition: color 0.2s;

    &:hover {
      color: #666;
    }
  }

  .popup-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
    padding-right: 20px;
  }

  .popup-device-name {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .popup-status-tag {
    flex-shrink: 0;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;

    &.tag-active {
      background: rgba(67, 207, 124, 0.12);
      color: #43cf7c;
    }

    &.tag-danger {
      background: rgba(250, 55, 88, 0.1);
      color: #fa3758;
    }
  }

  .popup-body {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .popup-row {
    display: flex;
    font-size: 12px;
    gap: 8px;
  }

  .popup-label {
    color: #999;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .popup-value {
    color: #333;
    word-break: break-all;

    &.mono {
      font-family: 'SFMono-Regular', Consolas, monospace;
    }
  }

  /* ========== 弹窗过渡动画 ========== */
  .popup-fade-enter-active,
  .popup-fade-leave-active {
    transition: all 0.2s ease;
  }

  .popup-fade-enter-from,
  .popup-fade-leave-to {
    opacity: 0;
    transform: translate(-50%, -100%) scale(0.95);
  }
</style>
