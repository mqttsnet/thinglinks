<template>
  <div class="live-monitor">
    <!-- Left: Source Tree Panel -->
    <div class="live-monitor__left" :class="{ 'live-monitor__left--collapsed': leftCollapsed }">
      <div v-show="!leftCollapsed" class="live-monitor__left-content">
        <SourceTree @play-source="handlePlaySource" />
      </div>
      <div class="live-monitor__left-toggle" @click="leftCollapsed = !leftCollapsed">
        <Icon :icon="leftCollapsed ? 'ant-design:right-outlined' : 'ant-design:left-outlined'" :size="12" />
      </div>
    </div>

    <!-- Center: Split-Screen Grid -->
    <div class="live-monitor__center">
      <!-- Top Toolbar -->
      <div class="live-monitor__toolbar">
        <div class="live-monitor__toolbar-left">
          <span class="live-monitor__toolbar-title">
            <Icon icon="ant-design:appstore-outlined" :size="16" color="#5d87ff" />
            {{ t('video.device.live.table.title') }}
          </span>
          <a-tag color="blue">{{ playingCount }} / {{ currentLayout }}</a-tag>
        </div>
        <div class="live-monitor__toolbar-right">
          <!-- Layout Switcher -->
          <a-radio-group v-model:value="currentLayout" size="small" button-style="solid" class="live-monitor__layout-switch">
            <a-tooltip :title="t('video.device.live.grid.layout1')">
              <a-radio-button :value="1">
                <Icon icon="ant-design:border-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip :title="t('video.device.live.grid.layout4')">
              <a-radio-button :value="4">
                <Icon icon="ant-design:appstore-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip :title="t('video.device.live.grid.layout9')">
              <a-radio-button :value="9">
                <Icon icon="ant-design:table-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip :title="t('video.device.live.grid.layout16')">
              <a-radio-button :value="16">
                <Icon icon="ant-design:block-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip title="1+2">
              <a-radio-button value="3-1">
                <Icon icon="ant-design:pic-left-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
            <a-tooltip title="1+3">
              <a-radio-button value="4-1">
                <Icon icon="ant-design:layout-outlined" :size="14" />
              </a-radio-button>
            </a-tooltip>
          </a-radio-group>

          <!-- Auto Fill -->
          <a-tooltip :title="t('video.device.live.grid.autoFillDesc')">
            <a-button size="small" type="primary" ghost @click="handleAutoFill" :loading="autoFilling">
              <template #icon><Icon icon="ant-design:thunderbolt-outlined" :size="14" /></template>
              {{ t('video.device.live.grid.autoFill') }}
            </a-button>
          </a-tooltip>

          <!-- Player Type Global -->
          <a-select
            v-model:value="globalPlayerType"
            size="small"
            :options="playerTypeOptions"
            style="width: 110px"
            @change="onGlobalPlayerChange"
          />

          <!-- Fullscreen Toggle -->
          <a-tooltip :title="t('video.device.live.grid.fullscreen')">
            <a-button size="small" @click="toggleFullscreen">
              <template #icon>
                <Icon :icon="isFullscreen ? 'ant-design:fullscreen-exit-outlined' : 'ant-design:fullscreen-outlined'" :size="14" />
              </template>
            </a-button>
          </a-tooltip>

          <!-- Keyboard Shortcut Help -->
          <a-tooltip :title="t('video.device.live.shortcut.title')">
            <a-button size="small" @click="showShortcutHelp = !showShortcutHelp">
              <template #icon><Icon icon="ant-design:info-circle-outlined" :size="14" /></template>
            </a-button>
          </a-tooltip>

          <!-- Clear All -->
          <a-tooltip :title="t('video.device.live.grid.clearAll')">
            <a-button size="small" danger @click="handleClearAll">
              <template #icon><Icon icon="ant-design:clear-outlined" :size="14" /></template>
            </a-button>
          </a-tooltip>
        </div>
      </div>

      <!-- Shortcut Help Banner -->
      <div v-if="showShortcutHelp" class="live-monitor__shortcut-help">
        <div class="live-monitor__shortcut-help-content">
          <span><kbd>&larr;</kbd><kbd>&uarr;</kbd><kbd>&darr;</kbd><kbd>&rarr;</kbd> {{ t('video.device.live.shortcut.arrowKeys') }}</span>
          <span><kbd>Enter</kbd> {{ t('video.device.live.shortcut.enter') }}</span>
          <span><kbd>Delete</kbd> {{ t('video.device.live.shortcut.delete') }}</span>
          <span><kbd>Esc</kbd> {{ t('video.device.live.shortcut.escape') }}</span>
          <span><kbd>1</kbd><kbd>4</kbd><kbd>9</kbd> {{ t('video.device.live.shortcut.number') }}</span>
        </div>
        <Icon icon="ant-design:close-outlined" :size="12" class="live-monitor__shortcut-close" @click="showShortcutHelp = false" />
      </div>

      <!-- Video Grid -->
      <div
        ref="gridRef"
        class="live-monitor__grid"
        :class="[gridLayoutClass, { 'live-monitor__grid--maximized': maximizedIndex !== null }]"
        tabindex="0"
        @keydown="onKeyDown"
      >
        <VideoCell
          v-for="cell in visibleCells"
          :ref="(el: any) => { if (el) cellRefs[cell.index] = el; }"
          :key="cell.index"
          :cellIndex="cell.index"
          :source="cell.source"
          :active="activeIndex === cell.index"
          :playerType="globalPlayerType"
          :playUrl="cell.playUrl"
          :status="cell.status"
          :errorMsg="cell.errorMsg"
          :layout="maximizedIndex !== null ? 1 : cellCount"
          :class="{ 'video-cell--maximized': maximizedIndex === cell.index }"
          @select="handleCellSelect(cell.index)"
          @close="handleCellClose(cell.index)"
          @drop="(source) => handleCellDrop(cell.index, source)"
          @swap="(fromIndex) => handleCellSwap(cell.index, fromIndex)"
          @retry="handleCellRetry(cell.index)"
          @show-urls="handleShowUrls(cell.index)"
          @dblclick="handleCellDblClick(cell.index)"
        />
      </div>
    </div>

    <!-- Right: Control Panel -->
    <div class="live-monitor__right" :class="{ 'live-monitor__right--collapsed': rightCollapsed }">
      <div class="live-monitor__right-toggle" @click="rightCollapsed = !rightCollapsed">
        <Icon :icon="rightCollapsed ? 'ant-design:left-outlined' : 'ant-design:right-outlined'" :size="12" />
      </div>
      <div v-show="!rightCollapsed" class="live-monitor__right-content">
        <ControlPanel
          :activeSource="activeCell?.source ?? null"
          v-model:playerType="globalPlayerType"
          :streamInfo="activeCell?.streamInfo ?? null"
          @snapshot="handleSnapshot"
          @fullscreen="handleCellFullscreen"
          @close-cell="handleCloseActiveCell"
        />
      </div>
    </div>

    <!-- Stream URLs Modal -->
    <BasicModal
      @register="registerUrlModal"
      :title="t('video.device.live.streamUrls')"
      :footer="null"
      :destroyOnClose="true"
      :width="700"
    >
      <div v-if="urlModalInfo" class="url-modal">
        <a-descriptions bordered size="small" :column="1">
          <template v-for="(urlObj, protocol) in urlModalUrls" :key="protocol">
            <a-descriptions-item v-if="urlObj" :label="protocol">
              <a-typography-paragraph
                :copyable="{ text: urlObj }"
                :ellipsis="true"
                :content="urlObj"
                style="margin-bottom: 0"
              />
            </a-descriptions-item>
          </template>
        </a-descriptions>
      </div>
      <a-empty v-else :description="t('video.device.live.noStream')" />
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
  import { useDebounceFn } from '@vueuse/core';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Icon } from '/@/components/Icon';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { playStart, playStop } from '/@/api/video/stream/play';
  import { getPlayUrl as proxyGetPlayUrl } from '/@/api/video/media/proxy';
  import { getPlayUrl as pushGetPlayUrl } from '/@/api/video/media/push';
  import { page as channelPage } from '/@/api/video/device/channel';
  import SourceTree from './components/SourceTree.vue';
  import VideoCell from './components/VideoCell.vue';
  import ControlPanel from './components/ControlPanel.vue';
  import type { PlayerType } from '/@/components/video/player/types';
  import type { StreamSource, GridCell, LayoutType, CellStatus } from './types';

  export default defineComponent({
    name: 'VideoDeviceLive',
    components: { Icon, BasicModal, SourceTree, VideoCell, ControlPanel },
    setup() {
      const { t } = useI18n();
      const { createConfirm, createMessage } = useMessage();
      const [registerUrlModal, { openModal: openUrlModal }] = useModal();

      // ==================== Layout State ====================
      const currentLayout = ref<LayoutType>(4);
      const leftCollapsed = ref(false);
      const rightCollapsed = ref(false);
      const isFullscreen = ref(false);
      const gridRef = ref<HTMLElement | null>(null);
      const cellRefs: Record<number, any> = {};
      const globalPlayerType = ref<PlayerType>('jessibuca');
      const activeIndex = ref(0);

      const showShortcutHelp = ref(false);
      const maximizedIndex = ref<number | null>(null);
      const autoFilling = ref(false);

      const gridCols = computed(() => {
        if (maximizedIndex.value !== null) return 1;
        switch (currentLayout.value) {
          case 1: return 1;
          case 4: return 2;
          case 9: return 3;
          case 16: return 4;
          case '3-1': return 2;
          case '4-1': return 2;
          default: return 2;
        }
      });

      const cellCount = computed(() => getCellCount(currentLayout.value));

      // CSS Grid 布局类名
      const gridLayoutClass = computed(() => {
        if (maximizedIndex.value !== null) return 'live-monitor__grid--maximized';
        const layout = currentLayout.value;
        if (layout === '3-1') return 'live-monitor__grid--layout-3-1';
        if (layout === '4-1') return 'live-monitor__grid--layout-4-1';
        return `live-monitor__grid--layout-${gridCols.value}`;
      });

      // 根据布局获取单元格数量
      function getCellCount(layout: LayoutType): number {
        if (layout === '3-1') return 3;
        if (layout === '4-1') return 4;
        return layout as number;
      }

      const playerTypeOptions = [
        { label: 'Jessibuca', value: 'jessibuca' },
        { label: 'FLV', value: 'flv' },
        { label: 'HLS', value: 'hls' },
      ];

      const visibleCells = computed(() => {
        if (maximizedIndex.value !== null) {
          const cell = cells.value[maximizedIndex.value];
          return cell ? [cell] : cells.value;
        }
        return cells.value;
      });

      // ==================== Grid Cells ====================
      const cells = ref<GridCell[]>([]);

      function initCells(count: number) {
        const oldCells = cells.value;
        const newCells: GridCell[] = [];
        for (let i = 0; i < count; i++) {
          if (i < oldCells.length && oldCells[i].source) {
            // Preserve existing playing cells
            newCells.push({ ...oldCells[i], index: i });
          } else {
            newCells.push(createEmptyCell(i));
          }
        }
        // Stop streams for cells that are being removed
        for (let i = count; i < oldCells.length; i++) {
          if (oldCells[i].source) {
            stopCellStream(oldCells[i]);
          }
        }
        cells.value = newCells;
        // Clean stale cellRefs for removed cells
        for (let i = count; i < oldCells.length; i++) {
          delete cellRefs[i];
        }
        // Adjust active index
        if (activeIndex.value >= count) {
          activeIndex.value = 0;
        }
      }

      function createEmptyCell(index: number): GridCell {
        return {
          index,
          source: null,
          playUrl: '',
          status: 'empty' as CellStatus,
          errorMsg: '',
          streamInfo: null,
        };
      }

      // Initialize with 4 cells
      initCells(getCellCount(4));

      watch(currentLayout, (val) => {
        maximizedIndex.value = null;
        initCells(getCellCount(val));
      });

      const activeCell = computed(() => {
        return cells.value[activeIndex.value] || null;
      });

      const playingCount = computed(() => {
        return cells.value.filter((c) => c.source && c.status === 'playing').length;
      });

      // ==================== Stream Play Logic ====================
      async function startStream(cell: GridCell, source: StreamSource) {
        cell.source = source;
        cell.status = 'loading';
        cell.errorMsg = '';
        cell.streamInfo = null;
        cell.playUrl = '';

        try {
          if (source.sourceType === 'channel') {
            // GB28181 device channel
            const res = await playStart({
              deviceIdentification: source.deviceIdentification!,
              channelIdentification: source.channelIdentification!,
            });
            cell.streamInfo = res;
            cell.playUrl = resolvePlayUrl(res, globalPlayerType.value);
            cell.status = cell.playUrl ? 'playing' : 'error';
            if (!cell.playUrl) {
              cell.errorMsg = t('video.device.live.noStream');
            }
          } else if (source.sourceType === 'proxy') {
            // Proxy stream
            const res = await proxyGetPlayUrl(source.id || source.proxyId!);
            cell.streamInfo = res;
            cell.playUrl = resolveProxyPushUrl(res, globalPlayerType.value);
            cell.status = cell.playUrl ? 'playing' : 'error';
            if (!cell.playUrl) {
              cell.errorMsg = t('video.device.live.noStream');
            }
          } else if (source.sourceType === 'push') {
            // Push stream
            const res = await pushGetPlayUrl(source.id || source.pushId!);
            cell.streamInfo = res;
            cell.playUrl = resolveProxyPushUrl(res, globalPlayerType.value);
            cell.status = cell.playUrl ? 'playing' : 'error';
            if (!cell.playUrl) {
              cell.errorMsg = t('video.device.live.noStream');
            }
          }
        } catch (e: any) {
          cell.status = 'error';
          cell.errorMsg = e?.message || t('video.device.live.playFailed');
        }
      }

      const isHttps = window.location.protocol.includes('https');

      /** Resolve URL from PlayResultVO (channel play start) */
      function resolvePlayUrl(info: any, playerType: PlayerType): string {
        if (!info) return '';
        switch (playerType) {
          case 'jessibuca':
            return (isHttps ? info.wssFlv?.url : info.wsFlv?.url)
              || info.wsFlv?.url || info.flv?.url || info.hls?.url || '';
          case 'flv':
            return (isHttps ? info.httpsFlv?.url : info.flv?.url)
              || info.flv?.url || '';
          case 'hls':
            return (isHttps ? info.httpsHls?.url : info.hls?.url)
              || info.hls?.url || '';
          default:
            return info.flv?.url || info.hls?.url || '';
        }
      }

      /** Resolve URL from PlayUrlResultVO (proxy/push getPlayUrl) */
      function resolveProxyPushUrl(info: any, playerType: PlayerType): string {
        const list = info?.zlmMediaServerStreamInfoList;
        if (!list?.length) return '';
        const first = list[0];
        if (!first) return '';
        switch (playerType) {
          case 'jessibuca':
            return (isHttps ? first.wssFlv : first.wsFlv)
              || first.wsFlv || first.flv || first.hls || '';
          case 'flv':
            return (isHttps ? first.httpsFlv : first.flv)
              || first.flv || '';
          case 'hls':
            return (isHttps ? first.httpsHls : first.hls)
              || first.hls || '';
          default:
            return first.flv || first.hls || '';
        }
      }

      async function stopCellStream(cell: GridCell) {
        if (cell.source?.sourceType === 'channel' && cell.source.deviceIdentification) {
          try {
            await playStop(
              cell.source.deviceIdentification,
              cell.source.channelIdentification!,
            );
          } catch (e) {
            console.warn('[stopCellStream] playStop failed:', e);
          }
        }
        // Proxy/push: no explicit stop needed
      }

      // ==================== Event Handlers ====================
      function handleCellSelect(index: number) {
        activeIndex.value = index;
      }

      async function handleCellDrop(index: number, source: StreamSource) {
        const cell = cells.value[index];
        if (!cell) return;

        // If cell already has a stream, stop it first
        if (cell.source) {
          await stopCellStream(cell);
        }

        // Start new stream
        activeIndex.value = index;
        await startStream(cell, source);
      }

      function handleCellSwap(toIndex: number, fromIndex: number) {
        const cellA = cells.value[fromIndex];
        const cellB = cells.value[toIndex];
        if (!cellA || !cellB || fromIndex === toIndex) return;

        // 交换两个单元格的全部状态
        const tempSource = cellA.source;
        const tempPlayUrl = cellA.playUrl;
        const tempStatus = cellA.status;
        const tempErrorMsg = cellA.errorMsg;
        const tempStreamInfo = cellA.streamInfo;

        cellA.source = cellB.source;
        cellA.playUrl = cellB.playUrl;
        cellA.status = cellB.status;
        cellA.errorMsg = cellB.errorMsg;
        cellA.streamInfo = cellB.streamInfo;

        cellB.source = tempSource;
        cellB.playUrl = tempPlayUrl;
        cellB.status = tempStatus;
        cellB.errorMsg = tempErrorMsg;
        cellB.streamInfo = tempStreamInfo;
      }

      async function handleCellClose(index: number) {
        const cell = cells.value[index];
        if (!cell || !cell.source) return;

        await stopCellStream(cell);
        // Reset cell
        Object.assign(cell, createEmptyCell(index));
      }

      async function handleCellRetry(index: number) {
        const cell = cells.value[index];
        if (!cell?.source) return;
        const source = { ...cell.source };
        await startStream(cell, source);
      }

      async function handleClearAll() {
        createConfirm({
          iconType: 'warning',
          content: t('video.device.live.grid.confirmClearAll'),
          onOk: async () => {
            for (const cell of cells.value) {
              if (cell.source) {
                await stopCellStream(cell);
                Object.assign(cell, createEmptyCell(cell.index));
              }
            }
          },
        });
      }

      function handleCloseActiveCell() {
        handleCellClose(activeIndex.value);
      }

      function handleSnapshot() {
        const cellComp = cellRefs[activeIndex.value];
        if (cellComp?.snapshot) {
          cellComp.snapshot();
          createMessage.success(t('video.device.live.snapshot'));
        } else {
          createMessage.warning(t('video.device.live.noStream'));
        }
      }

      function handleCellFullscreen() {
        // Use the grid ref to go fullscreen on the active cell
        const cellEls = gridRef.value?.querySelectorAll('.video-cell');
        if (cellEls?.[activeIndex.value]) {
          const el = cellEls[activeIndex.value] as HTMLElement;
          if (el.requestFullscreen) {
            el.requestFullscreen();
          }
        }
      }

      // ==================== Stream URL Modal ====================
      const urlModalInfo = ref<any>(null);
      const urlModalUrls = computed(() => {
        const info = urlModalInfo.value;
        if (!info) return {};
        const map: Record<string, string> = {};
        // PlayResultVO format
        if (info.flv?.url) map['HTTP-FLV'] = info.flv.url;
        if (info.wsFlv?.url) map['WS-FLV'] = info.wsFlv.url;
        if (info.hls?.url) map['HLS'] = info.hls.url;
        if (info.rtmp?.url) map['RTMP'] = info.rtmp.url;
        if (info.rtsp?.url) map['RTSP'] = info.rtsp.url;
        if (info.rtc?.url) map['WebRTC'] = info.rtc.url;
        if (info.fmp4?.url) map['FMP4'] = info.fmp4.url;
        // PlayUrlResultVO format
        if (info.zlmMediaServerStreamInfoList?.length) {
          const first = info.zlmMediaServerStreamInfoList[0];
          if (first.flv) map['HTTP-FLV'] = first.flv;
          if (first.wsFlv) map['WS-FLV'] = first.wsFlv;
          if (first.hls) map['HLS'] = first.hls;
          if (first.rtmp) map['RTMP'] = first.rtmp;
          if (first.rtsp) map['RTSP'] = first.rtsp;
        }
        return map;
      });

      function handleShowUrls(index: number) {
        const cell = cells.value[index];
        if (cell?.streamInfo) {
          urlModalInfo.value = cell.streamInfo;
          openUrlModal(true);
        }
      }

      // ==================== Click to play from SourceTree ====================
      function handlePlaySource(source: StreamSource) {
        // Find the first empty cell, or the active cell
        let targetIndex = cells.value.findIndex((c) => !c.source);
        if (targetIndex === -1) {
          // No empty cell, use active cell
          targetIndex = activeIndex.value;
        }
        handleCellDrop(targetIndex, source);
      }

      // ==================== Double-click Maximize ====================
      function handleCellDblClick(index: number) {
        if (maximizedIndex.value === index) {
          maximizedIndex.value = null;
        } else {
          maximizedIndex.value = index;
        }
      }

      // ==================== Auto-Fill ====================
      async function handleAutoFill() {
        autoFilling.value = true;
        try {
          // Load online channels
          const result = await channelPage({
            model: { onlineStatus: true },
            size: 100,
            current: 1,
          } as any);
          const channels = result?.records || [];
          if (channels.length === 0) {
            createMessage.warning(t('video.device.live.grid.noOnlineChannel'));
            return;
          }

          let filledCount = 0;
          let channelIdx = 0;
          for (const cell of cells.value) {
            if (!cell.source && channelIdx < channels.length) {
              const ch = channels[channelIdx++];
              const source: StreamSource = {
                sourceType: 'channel',
                deviceIdentification: ch.deviceIdentification,
                channelIdentification: ch.channelIdentification,
                channelName: ch.channelName,
                hasAudio: ch.hasAudio,
                ptzCapability: ch.ptzCapability,
              };
              // Start stream (fire and forget for parallel loading)
              startStream(cell, source);
              filledCount++;
            }
          }

          if (filledCount > 0) {
            createMessage.success(
              t('video.device.live.grid.autoFilled').replace('{count}', String(filledCount)),
            );
          } else {
            createMessage.info(t('video.device.live.grid.noOnlineChannel'));
          }
        } catch (e: any) {
          console.warn('Auto fill error:', e);
          createMessage.error(e?.message || 'Auto fill failed');
        } finally {
          autoFilling.value = false;
        }
      }

      // ==================== Keyboard Shortcuts ====================
      function onKeyDown(e: KeyboardEvent) {
        const cols = gridCols.value;
        const total = cells.value.length;

        switch (e.key) {
          case 'ArrowLeft':
            e.preventDefault();
            activeIndex.value = Math.max(0, activeIndex.value - 1);
            break;
          case 'ArrowRight':
            e.preventDefault();
            activeIndex.value = Math.min(total - 1, activeIndex.value + 1);
            break;
          case 'ArrowUp':
            e.preventDefault();
            activeIndex.value = Math.max(0, activeIndex.value - cols);
            break;
          case 'ArrowDown':
            e.preventDefault();
            activeIndex.value = Math.min(total - 1, activeIndex.value + cols);
            break;
          case 'Enter':
            e.preventDefault();
            handleCellDblClick(activeIndex.value);
            break;
          case 'Delete':
          case 'Backspace':
            e.preventDefault();
            handleCellClose(activeIndex.value);
            break;
          case 'Escape':
            e.preventDefault();
            if (maximizedIndex.value !== null) {
              maximizedIndex.value = null;
            } else if (isFullscreen.value) {
              document.exitFullscreen?.();
            }
            break;
          case '1':
            if (!e.ctrlKey && !e.metaKey) {
              e.preventDefault();
              currentLayout.value = 1;
            }
            break;
          case '2':
            if (!e.ctrlKey && !e.metaKey) {
              e.preventDefault();
              currentLayout.value = 4;
            }
            break;
          case '3':
            if (!e.ctrlKey && !e.metaKey) {
              e.preventDefault();
              currentLayout.value = 9;
            }
            break;
          case '4':
            if (!e.ctrlKey && !e.metaKey) {
              e.preventDefault();
              currentLayout.value = 16;
            }
            break;
        }
      }

      // Focus the grid on mount for keyboard events
      onMounted(() => {
        setTimeout(() => {
          gridRef.value?.focus({ preventScroll: true });
        }, 300);
      });

      // ==================== Fullscreen ====================
      function toggleFullscreen() {
        const el = document.querySelector('.live-monitor') as HTMLElement;
        if (!el) return;
        if (!document.fullscreenElement) {
          el.requestFullscreen?.();
          isFullscreen.value = true;
        } else {
          document.exitFullscreen?.();
          isFullscreen.value = false;
        }
      }

      // Listen for fullscreen change
      function onFullscreenChange() {
        isFullscreen.value = !!document.fullscreenElement;
      }
      document.addEventListener('fullscreenchange', onFullscreenChange);

      // ==================== Player Type Change ====================
      const onGlobalPlayerChange = useDebounceFn(() => {
        // Re-resolve play URLs for all playing cells
        for (const cell of cells.value) {
          if (cell.source && cell.streamInfo && cell.status === 'playing') {
            if (cell.source.sourceType === 'channel') {
              cell.playUrl = resolvePlayUrl(cell.streamInfo, globalPlayerType.value);
            } else {
              cell.playUrl = resolveProxyPushUrl(cell.streamInfo, globalPlayerType.value);
            }
          }
        }
      }, 300);

      // ==================== Cleanup ====================
      onBeforeUnmount(() => {
        document.removeEventListener('fullscreenchange', onFullscreenChange);
        // Stop all active streams
        for (const cell of cells.value) {
          if (cell.source) {
            stopCellStream(cell);
          }
        }
      });

      return {
        t,
        // Layout
        currentLayout,
        leftCollapsed,
        rightCollapsed,
        isFullscreen,
        gridRef,
        cellRefs,
        gridCols,
        gridLayoutClass,
        cellCount,
        globalPlayerType,
        activeIndex,
        playerTypeOptions,
        showShortcutHelp,
        maximizedIndex,
        autoFilling,
        visibleCells,
        // Cells
        cells,
        activeCell,
        playingCount,
        // Handlers
        handleCellSelect,
        handleCellDrop,
        handleCellSwap,
        handleCellClose,
        handleCellRetry,
        handleClearAll,
        handleCloseActiveCell,
        handleSnapshot,
        handleCellFullscreen,
        handleShowUrls,
        handlePlaySource,
        handleCellDblClick,
        handleAutoFill,
        onKeyDown,
        toggleFullscreen,
        onGlobalPlayerChange,
        // URL Modal
        registerUrlModal,
        urlModalInfo,
        urlModalUrls,
      };
    },
  });
</script>

<style lang="less" scoped>
  @primary: #5d87ff;
  @success: #13deb9;
  @warning: #ffae1f;
  @danger: #fa896b;
  @purple: #7c5cfc;
  @shadow-card: 0 2px 12px rgba(0, 0, 0, 0.04);

  .live-monitor {
    display: flex;
    height: calc(100vh - 78px);
    gap: 0;
    background: #f5f7fa;
    overflow: hidden;

    // ==================== Left Panel ====================
    &__left {
      position: relative;
      width: 280px;
      min-width: 280px;
      display: flex;
      transition: all 0.3s ease;
      z-index: 10;

      &--collapsed {
        width: 0;
        min-width: 0;

        .live-monitor__left-content {
          opacity: 0;
          pointer-events: none;
        }
      }

      &-content {
        flex: 1;
        overflow: hidden;
        padding: 8px 0 8px 8px;
        transition: opacity 0.2s;
      }

      &-toggle {
        position: absolute;
        right: -14px;
        top: 50%;
        transform: translateY(-50%);
        width: 14px;
        height: 48px;
        background: #fff;
        border-radius: 0 8px 8px 0;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
        z-index: 11;
        color: #8c97a5;

        &:hover {
          color: @primary;
          background: #f0f4ff;
        }
      }
    }

    // ==================== Center Panel ====================
    &__center {
      flex: 1;
      display: flex;
      flex-direction: column;
      padding: 8px;
      min-width: 0;
    }

    &__toolbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: #fff;
      border-radius: 10px;
      box-shadow: @shadow-card;
      margin-bottom: 8px;
      flex-shrink: 0;

      &-left {
        display: flex;
        align-items: center;
        gap: 10px;
      }

      &-title {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #2a3547;
      }

      &-right {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    &__layout-switch {
      :deep(.ant-radio-button-wrapper) {
        padding: 0 8px;

        &-checked {
          background: @primary;
          border-color: @primary;
        }
      }
    }

    &__shortcut-help {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 6px 14px;
      background: fade(@primary, 8%);
      border: 1px solid fade(@primary, 15%);
      border-radius: 8px;
      margin-bottom: 6px;
      flex-shrink: 0;

      &-content {
        display: flex;
        align-items: center;
        gap: 16px;
        flex-wrap: wrap;
        font-size: 12px;
        color: #5a6a85;

        kbd {
          display: inline-block;
          padding: 1px 5px;
          font-size: 11px;
          font-family: monospace;
          color: @primary;
          background: #fff;
          border: 1px solid fade(@primary, 20%);
          border-radius: 3px;
          margin: 0 2px;
        }
      }
    }

    &__shortcut-close {
      cursor: pointer;
      color: #8c97a5;

      &:hover {
        color: @danger;
      }
    }

    &__grid {
      flex: 1;
      display: grid;
      gap: 4px;
      border-radius: 10px;
      overflow: hidden;
      min-height: 0;
      outline: none;

      &--layout-1 {
        grid-template-columns: 1fr;
        grid-template-rows: 1fr;
      }

      &--layout-2 {
        grid-template-columns: repeat(2, 1fr);
        grid-template-rows: repeat(2, 1fr);
      }

      &--layout-3 {
        grid-template-columns: repeat(3, 1fr);
        grid-template-rows: repeat(3, 1fr);
      }

      &--layout-4 {
        grid-template-columns: repeat(4, 1fr);
        grid-template-rows: repeat(4, 1fr);
      }

      // 不规则布局 3-1: 左1大 + 右2小
      &--layout-3-1 {
        grid-template-columns: 2fr 1fr;
        grid-template-rows: 1fr 1fr;

        .video-cell:first-child {
          grid-row: 1 / 3;
        }
      }

      // 不规则布局 4-1: 左1大 + 右3小
      &--layout-4-1 {
        grid-template-columns: 2fr 1fr;
        grid-template-rows: 1fr 1fr 1fr;

        .video-cell:first-child {
          grid-row: 1 / 4;
        }
      }

      &--maximized {
        grid-template-columns: 1fr !important;
        grid-template-rows: 1fr !important;
      }
    }

    // ==================== Right Panel ====================
    &__right {
      position: relative;
      width: 280px;
      min-width: 280px;
      display: flex;
      transition: all 0.3s ease;
      z-index: 10;

      &--collapsed {
        width: 0;
        min-width: 0;

        .live-monitor__right-content {
          opacity: 0;
          pointer-events: none;
        }
      }

      &-content {
        flex: 1;
        overflow: hidden;
        padding: 8px 8px 8px 0;
        transition: opacity 0.2s;
      }

      &-toggle {
        position: absolute;
        left: -14px;
        top: 50%;
        transform: translateY(-50%);
        width: 14px;
        height: 48px;
        background: #fff;
        border-radius: 8px 0 0 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: -2px 0 8px rgba(0, 0, 0, 0.06);
        z-index: 11;
        color: #8c97a5;

        &:hover {
          color: @primary;
          background: #f0f4ff;
        }
      }
    }
  }

  // ==================== Fullscreen Mode ====================
  .live-monitor:fullscreen {
    background: #0a0e17;

    .live-monitor__toolbar {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);

      .live-monitor__toolbar-title {
        color: #fff;
      }
    }

    .live-monitor__left,
    .live-monitor__right {
      &-toggle {
        background: rgba(255, 255, 255, 0.1);
        color: rgba(255, 255, 255, 0.6);

        &:hover {
          background: rgba(255, 255, 255, 0.2);
          color: #fff;
        }
      }
    }
  }

  .url-modal {
    max-height: 400px;
    overflow-y: auto;
  }
</style>
