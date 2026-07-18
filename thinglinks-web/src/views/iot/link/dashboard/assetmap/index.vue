<template>
  <div class="map-wrap" style="position: relative;">
    <div ref="mapRef" class="map-container"></div>

    <!-- 右侧面板容器 -->
    <div class="panel-container">
    <!-- 搜索面板 -->
    <div class="search-panel" :class="{ collapsed: panelCollapsed }">
      <div v-if="panelCollapsed" class="collapsed-btns">
        <div class="panel-toggle-btn" @click="panelCollapsed = false">
          <SearchOutlined />
        </div>
        <div class="panel-toggle-btn config-btn" @click="configVisible = !configVisible">
          <SettingOutlined :class="{ spinning: configVisible }" />
        </div>
      </div>
      <template v-else>
        <div class="panel-header">
          <span class="panel-title">
            {{ t('iot.link.assetmap.assetmap.panelTitle') }}
            <span v-if="searchResults.length" class="panel-badge">{{ searchResults.length }}</span>
          </span>
          <span class="panel-header-actions">
            <span class="panel-collapse-btn" @click="configVisible = !configVisible">
              <SettingOutlined :class="{ spinning: configVisible }" />
            </span>
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
              @change="onFieldChange"
            />
            <Input
              v-model:value="searchKeyword"
              class="search-keyword-input"
              :placeholder="searchPlaceholder"
              allow-clear
              @input="onSearchInput"
              @change="onSearchChange"
            >
              <template #suffix>
                <SearchOutlined style="color: #bfbfbf; font-size: 13px;" />
              </template>
            </Input>
          </div>
        </div>
        <div class="panel-results">
          <div v-if="searchLoading" class="panel-loading">
            <LoadingOutlined spin />
            <span>{{ t('iot.link.assetmap.assetmap.searching') }}</span>
          </div>
          <div v-else-if="searchResults.length > 0" class="result-list">
            <div
              v-for="item in searchResults"
              :key="item.deviceIdentification"
              class="result-item"
              @click="onSelectDevice(item)"
            >
              <span
                class="status-dot"
                :class="{
                  online: item.connectStatus == 1,
                  offline: item.connectStatus == 2,
                  init: item.connectStatus == 0 || item.connectStatus == null,
                }"
              ></span>
              <div class="result-info">
                <div class="result-name">{{ item.deviceName || item.deviceIdentification }}</div>
                <div v-if="item.deviceName" class="result-id">{{ item.deviceIdentification }}</div>
              </div>
            </div>
          </div>
          <div v-else-if="searchKeyword && !searchLoading" class="panel-empty">
            {{ t('iot.link.assetmap.assetmap.noMatchDevice') }}
          </div>
          <div v-else class="panel-hint">
            {{ t('iot.link.assetmap.assetmap.inputKeyword') }}
          </div>
        </div>
      </template>
    </div>

    <!-- 重试配置卡片 -->
    <transition name="config-fade">
      <div v-if="configVisible" class="retry-config-card" :class="{ compact: panelCollapsed }">
        <div class="config-title">{{ t('iot.link.assetmap.assetmap.configTitle') }}</div>
        <div class="config-row">
          <span class="config-label">{{ t('iot.link.assetmap.assetmap.configMaxRetries') }}</span>
          <InputNumber
            v-model:value="retryConfig.maxRetries"
            :min="1"
            :max="10"
            :precision="0"
            size="small"
            class="config-input"
            @change="saveRetryConfig"
          />
          <span class="config-unit">{{ t('iot.link.assetmap.assetmap.configTimes') }}</span>
        </div>
        <div class="config-row">
          <span class="config-label">{{ t('iot.link.assetmap.assetmap.configRetryInterval') }}</span>
          <InputNumber
            v-model:value="retryConfig.retryInterval"
            :min="1"
            :max="60"
            :precision="0"
            size="small"
            class="config-input"
            @change="saveRetryConfig"
          />
          <span class="config-unit">s</span>
        </div>
      </div>
    </transition>

    <!-- 2D/3D 切换按钮 -->
    <div class="view-mode-btn" :class="{ active: is3D }" @click="toggleViewMode">
      {{ is3D ? '2D' : '3D' }}
    </div>
    </div><!-- /panel-container -->

    <!-- 加载指示器 -->
    <div v-if="dataLoading" class="loading-indicator">
      <LoadingOutlined spin />
      <span>{{ t('iot.link.assetmap.assetmap.loadingDeviceData') }} {{ loadProgress }}...</span>
    </div>

    <!-- 设备弹窗 -->
    <transition name="popup-fade">
      <div
        v-if="popup.visible"
        class="device-popup"
        :style="{ left: popup.x + 'px', top: popup.y + 'px' }"
      >
        <div class="popup-arrow"></div>
        <div class="popup-close" @click="closePopup">&times;</div>
        <!-- 加载骨架 -->
        <template v-if="popupLoading">
          <div class="popup-skeleton">
            <div class="sk-title"></div>
            <div class="sk-row"></div>
            <div class="sk-row short"></div>
            <div class="sk-row"></div>
          </div>
        </template>
        <!-- 内容 -->
        <template v-else>
          <div class="popup-header">
            <span class="popup-device-name" @click="goDeviceDetail">
              {{ device.deviceName }}
              <i class="iconfont icon-youjiantou1"></i>
            </span>
            <span class="popup-status-tag" :class="deviceStatusClass">{{ deviceStatus }}</span>
          </div>
          <div class="popup-body">
            <div class="popup-row">
              <span class="popup-label">{{ t('iot.link.device.device.nodeType') }}</span>
              <span class="popup-value">{{ device.deviceType }}</span>
            </div>
            <div class="popup-row">
              <span class="popup-label">{{ t('iot.link.device.device.deviceIdentification') }}</span>
              <span class="popup-value mono">{{ device.deviceIdentification }}</span>
            </div>
            <div class="popup-row">
              <span class="popup-label">{{ t('iot.link.assetmap.assetmap.currentPosition') }}</span>
              <span class="popup-value">{{ device.deviceLocationResultVO?.fullName || '-' }}</span>
            </div>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, reactive, computed, nextTick } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { useDebounceFn } from '@vueuse/core'
import { useRouter } from 'vue-router'
import { useDict } from '/@/components/Dict'
import { useI18n } from '/@/hooks/web/useI18n'
import { DictEnum } from '/@/enums/commonEnum'
import { getDeviceLocationPage } from '/@/api/iot/link/deviceLocation/deviceLocation'
import { detailBydeviceIdentification, getDeviceDetailsPage } from '/@/api/iot/link/device/device'
import { Input, Select, InputNumber } from 'ant-design-vue'
import { SearchOutlined, LoadingOutlined, MenuFoldOutlined, SettingOutlined } from '@ant-design/icons-vue'

const { getDictLabel, initGetDictList } = useDict()
const { t } = useI18n()
const { push } = useRouter()

const mapRef = ref(null)
let AMap = null
let map = null
let massMarks = null

const dataLoading = ref(false)
const loadedCount = ref(0)
const totalCount = ref(0)
const loadProgress = computed(() =>
  totalCount.value ? `${loadedCount.value}/${totalCount.value}` : '',
)

// =====================================================================
//  创建标记图标（Canvas 生成 data URL，无外部图片依赖）
// =====================================================================
function createMarkerIcon(color, w, h) {
  const canvas = document.createElement('canvas')
  const dpr = window.devicePixelRatio || 1
  const cw = w * dpr
  const ch = h * dpr
  canvas.width = cw
  canvas.height = ch
  const ctx = canvas.getContext('2d')
  const cx = cw / 2
  const r = cw * 0.42
  const circleY = r + dpr * 0.5
  const tipY = ch - dpr * 0.5

  // 纯色水滴，无立体效果
  drawDrop(ctx, cx, circleY, tipY, r, color)

  return canvas.toDataURL('image/png')
}

function drawDrop(ctx, cx, circleY, tipY, r, fillColor) {
  ctx.beginPath()
  ctx.moveTo(cx, tipY)
  ctx.bezierCurveTo(cx + r * 0.4, circleY + r * 0.95, cx + r, circleY + r * 0.4, cx + r, circleY)
  ctx.arc(cx, circleY, r, 0, Math.PI, true)
  ctx.bezierCurveTo(cx - r, circleY + r * 0.4, cx - r * 0.4, circleY + r * 0.95, cx, tipY)
  ctx.closePath()
  ctx.fillStyle = fillColor
  ctx.fill()
}

// =====================================================================
//  重试配置（localStorage 持久化）
// =====================================================================
const RETRY_CONFIG_KEY = 'assetmap_retry_config'
const DEFAULT_RETRY_CONFIG = { maxRetries: 3, retryInterval: 5 }

const configVisible = ref(false)
const is3D = ref(false)

const toggleViewMode = () => {
  if (!map) return
  is3D.value = !is3D.value
  map.setPitch(is3D.value ? 45 : 0, true)  // true = 动画过渡
}
const retryConfig = reactive({ ...DEFAULT_RETRY_CONFIG })

const loadRetryConfig = () => {
  try {
    const saved = localStorage.getItem(RETRY_CONFIG_KEY)
    if (saved) Object.assign(retryConfig, JSON.parse(saved))
  } catch (_) { /* 读取失败保持默认值 */ }
}

const saveRetryConfig = () => {
  try {
    localStorage.setItem(RETRY_CONFIG_KEY, JSON.stringify({ maxRetries: retryConfig.maxRetries, retryInterval: retryConfig.retryInterval }))
  } catch (_) { /* 存储失败静默处理 */ }
}

loadRetryConfig()


// ========== 搜索相关 ==========
const searchKeyword = ref('')
const searchResults = ref([])
const searchLoading = ref(false)
const panelCollapsed = ref(false)
let searchRequestId = 0

// 搜索字段选项（computed 支持切换语言自动更新）
const searchField = ref('deviceName')

const searchFieldOptions = computed(() => [
  { value: 'deviceName', label: t('iot.link.assetmap.assetmap.fieldDeviceName') },
  { value: 'deviceIdentification', label: t('iot.link.assetmap.assetmap.fieldDeviceIdentification') },
  { value: 'productIdentification', label: t('iot.link.assetmap.assetmap.fieldProductIdentification') },
])

const searchPlaceholder = computed(() => {
  const opt = searchFieldOptions.value.find((o) => o.value === searchField.value)
  return t('iot.link.assetmap.assetmap.searchPlaceholder').replace('{field}', opt?.label ?? '')
})

const doSearch = async (keyword) => {
  const kw = keyword.trim()
  if (!kw) {
    searchResults.value = []
    searchLoading.value = false
    return
  }
  const currentRequestId = ++searchRequestId
  searchLoading.value = true
  try {
    const res = await getDeviceDetailsPage({
      current: 1,
      size: 50,
      extra: {},
      model: { [searchField.value]: kw },
    })
    if (currentRequestId !== searchRequestId) return
    const list = []
    for (const item of res?.records || []) {
      if (!item.deviceIdentification) continue
      if (!item.deviceLocationResultVO?.longitude || !item.deviceLocationResultVO?.latitude) continue
      list.push(item)
      if (list.length >= 50) break
    }
    searchResults.value = list
  } catch (err) {
    console.error('搜索设备失败', err)
    if (currentRequestId === searchRequestId) searchResults.value = []
  } finally {
    if (currentRequestId === searchRequestId) searchLoading.value = false
  }
}

const debouncedSearch = useDebounceFn((kw) => doSearch(kw), 400)
const onSearchInput = () => debouncedSearch(searchKeyword.value)
const onSearchChange = (e) => {
  if (!e.target.value) {
    searchResults.value = []
    searchLoading.value = false
  }
}
// 切换字段时若已有关键词则重新搜索
const onFieldChange = () => {
  if (searchKeyword.value.trim()) doSearch(searchKeyword.value)
}

const onSelectDevice = (item) => {
  if (!map) return
  const lng = Number(item.deviceLocationResultVO.longitude)
  const lat = Number(item.deviceLocationResultVO.latitude)
  map.setZoomAndCenter(16, [lng, lat], false, 600)
  showPopupAt(lng, lat, item.deviceIdentification)
}

// ========== 弹窗相关 ==========
const popup = reactive({ visible: false, x: 0, y: 0, lng: null, lat: null })
const popupLoading = ref(false)
const device = ref({ deviceLocationResultVO: {} })
const deviceStatus = ref('')
const deviceStatusClass = ref('')

// LRU 缓存：最多 200 条，避免重复请求
const deviceCache = new Map()
const CACHE_MAX = 200

function cacheSet(key, val) {
  if (deviceCache.size >= CACHE_MAX) {
    // 删除最早的一条
    deviceCache.delete(deviceCache.keys().next().value)
  }
  deviceCache.set(key, val)
}

const closePopup = () => {
  popup.visible = false
  popupLoading.value = false
  device.value = { deviceLocationResultVO: {} }
  deviceStatus.value = ''
  deviceStatusClass.value = ''
}

function applyDeviceData(res) {
  device.value = {
    ...res,
    deviceType: getDictLabel('LINK_DEVICE_NODE_TYPE', res?.nodeType, ''),
  }
  deviceStatus.value = getDictLabel('LINK_DEVICE_STATUS', device.value?.deviceStatus, '')
  deviceStatusClass.value = { 1: 'tag-active', 2: 'tag-danger', 0: 'tag-default' }[device.value?.deviceStatus] || 'tag-default'
}

const showPopupAt = async (lng, lat, deviceIdentification) => {
  if (!deviceIdentification) return
  popup.lng = lng
  popup.lat = lat
  updatePopupPixel()

  // 命中缓存 → 立刻显示内容，零等待
  const cached = deviceCache.get(deviceIdentification)
  if (cached) {
    applyDeviceData(cached)
    popupLoading.value = false
    popup.visible = true
    return
  }

  // 未命中 → 立刻弹出骨架屏，异步加载
  popupLoading.value = true
  popup.visible = true

  try {
    const res = await detailBydeviceIdentification(deviceIdentification)
    cacheSet(deviceIdentification, res)
    applyDeviceData(res)
    popupLoading.value = false
    updatePopupPixel()
  } catch (err) {
    console.error('获取设备详情失败', err)
    popup.visible = false
    popupLoading.value = false
  }
}

function updatePopupPixel() {
  if (!map || popup.lng == null) return
  const pixel = map.lngLatToContainer(new AMap.LngLat(popup.lng, popup.lat))
  popup.x = Math.round(pixel.getX())
  popup.y = Math.round(pixel.getY()) - MARKER_H - 4 // 水滴顶部上方
}

const goDeviceDetail = () => {
  // 路由 :id 段语义为 deviceIdentification（业务唯一标识，非主键 id）
  if (device.value?.deviceIdentification) {
    push({ name: '设备详情', params: { id: device.value.deviceIdentification } })
  }
}

// =====================================================================
//  地图初始化
// =====================================================================
const MARKER_W = 14
const MARKER_H = 20

const initMap = async (key) => {
  AMap = await AMapLoader.load({
    key,
    version: '2.0',
    plugins: ['AMap.ToolBar', 'AMap.ControlBar', 'AMap.MassMarks'],
  })

  map = new AMap.Map(mapRef.value, {
    viewMode: '3D',   // 固定 3D 引擎，通过 pitch 控制 2D/3D 视角
    pitch: 0,         // 初始俯仰角 0 = 2D 平视
    zoom: 5,
    center: [104.5, 35.5],
    resizeEnable: true,
    mapStyle: 'amap://styles/normal',
    features: ['bg', 'road', 'building', 'point'],
  })

  // 控件
  map.addControl(new AMap.ToolBar({ position: { bottom: '40px', right: '20px' } }))

  // 创建水滴标记图标（主题色）
  const iconUrl = createMarkerIcon('#1890ff', MARKER_W, MARKER_H)

  // 海量标记（GPU 加速）
  massMarks = new AMap.MassMarks([], {
    zIndex: 111,
    zooms: [3, 20],
    cursor: 'pointer',
    style: {
      url: iconUrl,
      size: new AMap.Size(MARKER_W, MARKER_H),
      anchor: new AMap.Pixel(MARKER_W / 2, MARKER_H), // 锚点在水滴尖端
    },
  })

  // 标记点击 —— MassMarks 原生事件，无延迟
  massMarks.on('click', (e) => {
    const d = e.data
    const [lng, lat] = d.lnglat
    const currentZoom = map.getZoom()
    if (currentZoom < 14) {
      map.setZoomAndCenter(14, [lng, lat], false, 400)
    } else {
      map.setCenter([lng, lat], false, 400)
    }
    showPopupAt(lng, lat, d.did)
  })

  massMarks.setMap(map)

  // 点击空白关闭弹窗
  map.on('click', () => closePopup())

  // 弹窗跟随
  map.on('mapmove', updatePopupPixel)
  map.on('zoomchange', updatePopupPixel)
  map.on('resize', updatePopupPixel)

}

// =====================================================================
//  数据加载
// =====================================================================
let allMassData = []  // MassMarks 数据（纯 JS，不走响应式）
let isUnmounted = false

function processBatch(records) {
  for (let i = 0, len = records.length; i < len; i++) {
    const d = records[i]
    if (!d.longitude || !d.latitude) continue
    const lng = Number(d.longitude)
    const lat = Number(d.latitude)
    if (isNaN(lng) || isNaN(lat)) continue
    allMassData.push({
      lnglat: [lng, lat],
      did: d.deviceIdentification, // 仅存标识，最小化内存
    })
  }
}

/** 判断是否为超时错误 */
function isTimeoutError(err) {
  return (
    err?.code === 'ECONNABORTED' ||
    err?.code === 'ETIMEDOUT' ||
    err?.message?.toLowerCase().includes('timeout') ||
    err?.response?.status === 408
  )
}

/** 带重试的单页加载，失败时重试同一 page */
async function loadPageWithRetry(page, size) {
  let attempts = 0
  while (true) {
    try {
      return await getDeviceLocationPage({ current: page, size, model: {}, extra: {} })
    } catch (err) {
      if (isTimeoutError(err) && attempts < retryConfig.maxRetries && !isUnmounted) {
        attempts++
        await new Promise((resolve) => setTimeout(resolve, retryConfig.retryInterval * 1000))
        continue
      }
      throw err
    }
  }
}

const loadAllDeviceLocations = async () => {
  dataLoading.value = true
  let page = 1
  const size = 5000

  while (!isUnmounted) {
    const res = await loadPageWithRetry(page, size)
    const records = res.records || []
    totalCount.value = parseInt(res.total) || 0
    processBatch(records)
    loadedCount.value = allMassData.length

    // 首页立即渲染
    if (page === 1 && massMarks) {
      massMarks.setData(allMassData)
    }

    if (allMassData.length >= totalCount.value) break
    page++
  }

  // 全部加载完，最终渲染
  if (massMarks) {
    massMarks.setData(allMassData)
  }
  dataLoading.value = false
}

// =====================================================================
//  生命周期
// =====================================================================
onMounted(async () => {
  const dictList = await initGetDictList(DictEnum.TENANT_AMAP__AAPPLICATION_JS_API_KEY)
  const key = dictList.find((item) => item.key === 'key')?.name ?? ''
  if (!key) {
    console.error('未获取到高德 Key')
    return
  }
  await initMap(key)
  // 等 DOM 稳定后触发 resize，修复容器尺寸未就绪导致的顶部空白
  await nextTick()
  map?.resize()
  loadAllDeviceLocations()
})

onBeforeUnmount(() => {
  isUnmounted = true
  massMarks?.setMap(null)
  map?.destroy()
  allMassData = []
  deviceCache.clear()
})
</script>

<style lang="less" scoped>


@keyframes sk-pulse {
  0%, 100% { opacity: 1; }

  50% { opacity: 0.4; }
}

.map-wrap {
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

/* ========== 面板容器（统一定位） ========== */
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

  &.config-btn {
    font-size: 16px;
  }
}

.spinning {
  color: @primary-color;
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

/* ========== 重试配置卡片 ========== */
.retry-config-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  width: 340px;

  &.compact {
    width: auto;
    min-width: 200px;
  }
}

.config-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.config-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 8px;

  &:last-child {
    margin-bottom: 0;
  }
}

.config-label {
  flex: 1;
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

.config-input {
  width: 64px;

  :deep(.ant-input-number-input) {
    text-align: center;
    padding: 0 6px;
    font-size: 13px;
  }
}

.config-unit {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

/* 配置卡片过渡 */
.config-fade-enter-active,
.config-fade-leave-active {
  transition: all 0.2s ease;
}

.config-fade-enter-from,
.config-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
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
    box-shadow: 0 2px 12px rgb(15 23 42 / 12%);
  }
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
  padding: 0 12px 10px;
}

// 搜索框整体容器 —— 下拉 + 输入框合并为一个圆角矩形
.search-input-group {
  display: flex;
  align-items: stretch;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:focus-within {
    border-color: @primary-color;
    box-shadow: none;
  }

  // 字段选择下拉
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

  // 关键词输入框
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
    background-color: #fa3758;
    box-shadow: 0 0 4px rgba(250, 55, 88, 0.4);
  }

  &.init {
    background-color: #c0c4cc;
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: 'SF Mono', 'Menlo', monospace;
}

/* ========== 设备弹窗 ========== */
.device-popup {
  position: absolute;
  z-index: 999;
  transform: translate(-50%, -100%);
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px 14px;
  min-width: 300px;
  max-width: 380px;
  pointer-events: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1), 0 1px 4px rgba(0, 0, 0, 0.06);
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
  top: 10px;
  right: 14px;
  cursor: pointer;
  color: #c0c4cc;
  font-size: 20px;
  line-height: 1;
  transition: color 0.2s;

  &:hover {
    color: #666;
  }
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-right: 24px;
}

.popup-device-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  cursor: pointer;
  transition: color 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;

  &:hover {
    color: @primary-color;
  }

  .iconfont {
    font-size: 14px;
    margin-left: 4px;
  }
}

.popup-status-tag {
  flex-shrink: 0;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.tag-active {
  background: #f0faf4;
  color: #43cf7c;
  border: 1px solid #c3edcf;
}

.tag-danger {
  background: #fff1f0;
  color: #fa3758;
  border: 1px solid #ffc0c0;
}

.tag-default {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #e8e8e8;
}

.popup-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.popup-row {
  display: flex;
  align-items: baseline;
  font-size: 13px;
  line-height: 1.4;
}

.popup-label {
  color: #999;
  flex-shrink: 0;
  margin-right: 8px;
  white-space: nowrap;

  &::after {
    content: '：';
  }
}

.popup-value {
  color: #333;
  word-break: break-all;
}

.popup-value.mono {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 12px;
  color: #555;
}

/* 骨架屏 */
.popup-skeleton {
  padding: 2px 0;
}

.sk-title {
  width: 50%;
  height: 18px;
  background: #f0f0f0;
  border-radius: 4px;
  margin-bottom: 14px;
  animation: sk-pulse 1.2s ease-in-out infinite;
}

.sk-row {
  width: 100%;
  height: 13px;
  background: #f0f0f0;
  border-radius: 3px;
  margin-bottom: 10px;
  animation: sk-pulse 1.2s ease-in-out infinite;

  &.short {
    width: 65%;
  }

  &:last-child {
    margin-bottom: 0;
  }
}

/* 弹窗过渡 */
.popup-fade-enter-active {
  transition: all 0.2s ease-out;
}

.popup-fade-leave-active {
  transition: all 0.15s ease-in;
}

.popup-fade-enter-from {
  opacity: 0;
  transform: translate(-50%, -90%);
}

.popup-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -100%) scale(0.95);
}
</style>
