<template>
  <div class="source-tree">
    <!-- Header: Type selector + Search -->
    <div class="source-tree__header">
      <a-select
        v-model:value="sourceType"
        size="small"
        :options="sourceTypeOptions"
        class="source-tree__type-select"
        :getPopupContainer="(trigger: any) => trigger.parentNode"
      />
      <a-input
        v-model:value="searchText"
        :placeholder="searchPlaceholder"
        allow-clear
        size="small"
        class="source-tree__search"
      >
        <template #prefix>
          <Icon icon="ant-design:search-outlined" :size="14" color="#8c97a5" />
        </template>
      </a-input>
    </div>

    <!-- Content -->
    <div class="source-tree__content">
      <a-spin :spinning="loading" size="small">
        <!-- ==================== Group Type ==================== -->
        <template v-if="sourceType === 'group'">
          <div v-if="filteredGroupList.length" class="source-tree__list">
            <template v-for="group in filteredGroupList" :key="group.key">
              <div
                class="source-tree__group-header"
                @click="toggleGroup(group.key)"
              >
                <Icon
                  :icon="expandedGroups.has(group.key) ? 'ant-design:folder-open-outlined' : 'ant-design:folder-outlined'"
                  :size="14"
                  color="#5d87ff"
                />
                <span class="source-tree__group-name">{{ group.title }}</span>
                <span class="source-tree__node-count">{{ group.childCount }}</span>
                <Icon
                  :icon="expandedGroups.has(group.key) ? 'ant-design:up-outlined' : 'ant-design:down-outlined'"
                  :size="10"
                  class="source-tree__group-arrow"
                />
              </div>
              <template v-if="expandedGroups.has(group.key)">
                <div
                  v-for="ch in group.children || []"
                  :key="ch.key"
                  class="source-tree__list-item"
                  :class="{ 'source-tree__list-item--offline': !ch.online }"
                  draggable="true"
                  @dragstart="onDragStart($event, ch.sourceData)"
                  @click="handleSourceClick(ch.sourceData)"
                >
                  <Icon
                    icon="ant-design:video-camera-outlined"
                    :size="14"
                    :color="ch.online ? '#13deb9' : '#8c97a5'"
                    class="source-tree__list-item-left-icon"
                  />
                  <span class="source-tree__list-item-name">{{ ch.title }}</span>
                  <span class="source-tree__list-item-status" :class="ch.online ? 'online' : 'offline'" />
                </div>
              </template>
            </template>
          </div>
          <a-empty v-else :description="t('video.device.live.source.noData')" :image="simpleImage" />
        </template>

        <!-- ==================== Region Type ==================== -->
        <template v-else-if="sourceType === 'region'">
          <a-tree
            v-if="filteredRegionTree.length"
            :tree-data="filteredRegionTree"
            :field-names="{ title: 'title', key: 'key', children: 'children' }"
            :selectable="true"
            :load-data="onRegionLoadData"
            :virtual="true"
            :height="400"
            block-node
            @select="onRegionSelect"
          >
            <template #title="node">
              <div
                class="source-tree__region-node"
                :draggable="!!(node.isLeaf && node.sourceData)"
                @dragstart="node.isLeaf && node.sourceData && onDragStart($event, node.sourceData)"
              >
                <Icon
                  :icon="node.isLeaf && node.sourceData ? 'ant-design:video-camera-outlined' : 'ant-design:environment-outlined'"
                  :size="14"
                  :color="node.isLeaf && node.sourceData ? (node.online ? '#13deb9' : '#8c97a5') : '#ffae1f'"
                />
                <span :class="{ 'source-tree__node-title--offline': node.isLeaf && !node.online }">
                  {{ node.title }}
                </span>
                <span v-if="node.isLeaf && node.online" class="source-tree__node-status source-tree__node-status--online" />
              </div>
            </template>
          </a-tree>
          <a-empty v-else :description="t('video.device.live.source.noData')" :image="simpleImage" />
        </template>

        <!-- ==================== Proxy Type ==================== -->
        <template v-else-if="sourceType === 'proxy'">
          <div v-if="filteredProxyList.length" class="source-tree__list">
            <div
              v-for="item in filteredProxyList"
              :key="item.id"
              class="source-tree__list-item"
              draggable="true"
              @dragstart="onDragStart($event, makeProxySource(item))"
              @click="handleSourceClick(makeProxySource(item))"
            >
              <div class="source-tree__list-item-icon proxy">
                <Icon icon="ant-design:cloud-download-outlined" :size="16" />
              </div>
              <div class="source-tree__list-item-info">
                <span class="source-tree__list-item-name">{{ item.proxyName || item.streamIdentification }}</span>
                <span class="source-tree__list-item-desc">{{ item.url || item.srcUrl }}</span>
              </div>
              <span class="source-tree__list-item-status" :class="item.status ? 'online' : 'offline'" />
            </div>
          </div>
          <a-empty v-else :description="t('video.device.live.source.noData')" :image="simpleImage" />
        </template>

        <!-- ==================== Push Type ==================== -->
        <template v-else-if="sourceType === 'push'">
          <div v-if="filteredPushList.length" class="source-tree__list">
            <div
              v-for="item in filteredPushList"
              :key="item.id"
              class="source-tree__list-item"
              draggable="true"
              @dragstart="onDragStart($event, makePushSource(item))"
              @click="handleSourceClick(makePushSource(item))"
            >
              <div class="source-tree__list-item-icon push">
                <Icon icon="ant-design:cloud-upload-outlined" :size="16" />
              </div>
              <div class="source-tree__list-item-info">
                <span class="source-tree__list-item-name">{{ item.streamIdentification || item.appId }}</span>
                <span class="source-tree__list-item-desc">{{ item.appId }}</span>
              </div>
              <span class="source-tree__list-item-status" :class="{ online: item.pushIng, offline: !item.pushIng }" />
            </div>
          </div>
          <a-empty v-else :description="t('video.device.live.source.noData')" :image="simpleImage" />
        </template>
      </a-spin>
    </div>

    <!-- Stats Footer -->
    <div class="source-tree__footer">
      <span>{{ currentTypeLabel }}: {{ currentCount }}</span>
      <a-button type="link" size="small" @click="refreshCurrent">
        <Icon icon="ant-design:reload-outlined" :size="14" />
      </a-button>
    </div>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, computed, watch, onMounted } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { Icon } from '/@/components/Icon';
  import { Empty } from 'ant-design-vue';
  import { tree as groupTreeApi } from '/@/api/video/device/group';
  import { listByGroup } from '/@/api/video/device/groupRelation';
  import { page as channelPage } from '/@/api/video/device/channel';
  import { page as proxyPage } from '/@/api/video/media/proxy';
  import { page as pushPage } from '/@/api/video/media/push';
  import citiesData from '/@/utils/thinglinks/citiesGd.json';
  import { isTruthyStatus } from '/@/utils/thinglinks/common';
  import type { StreamSource } from '../types';

  /** Source type enum - maintained on frontend */
  type SourceFilterType = 'group' | 'region' | 'proxy' | 'push';

  export default defineComponent({
    name: 'SourceTree',
    components: { Icon },
    emits: ['play-source'],
    setup(_props, { emit }) {
      const { t } = useI18n();
      const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
      const searchText = ref('');
      const sourceType = ref<SourceFilterType>('group');
      const loading = ref(false);

      // Source type options (frontend enum)
      const sourceTypeOptions = computed(() => [
        { label: t('video.device.live.source.group'), value: 'group' },
        { label: t('video.device.live.source.region'), value: 'region' },
        { label: t('video.device.live.source.proxy'), value: 'proxy' },
        { label: t('video.device.live.source.push'), value: 'push' },
      ]);

      // Dynamic search placeholder based on type
      const searchPlaceholder = computed(() => {
        switch (sourceType.value) {
          case 'group':
            return t('video.device.live.source.searchChannel');
          case 'region':
            return t('video.device.live.source.searchRegion');
          case 'proxy':
            return t('video.device.live.source.searchProxy');
          case 'push':
            return t('video.device.live.source.searchPush');
          default:
            return t('video.device.live.source.search');
        }
      });

      // ==================== Click to play ====================
      function handleSourceClick(source: StreamSource | undefined) {
        if (!source) return;
        emit('play-source', source);
      }

      // ==================== Data cache per type ====================
      const dataLoaded = ref<Record<string, boolean>>({});

      // ==================== Device Group ====================
      const groupTreeData = ref<any[]>([]);
      const expandedGroups = ref(new Set<string>());

      function toggleGroup(key: string) {
        const s = new Set(expandedGroups.value);
        if (s.has(key)) {
          s.delete(key);
        } else {
          s.add(key);
        }
        expandedGroups.value = s;
      }

      async function loadGroupData() {
        loading.value = true;
        try {
          // Parallel: group tree + all channels
          const [groups, channelResult] = await Promise.all([
            groupTreeApi().catch(() => []),
            channelPage({ model: {}, size: 500, current: 1 } as any),
          ]);

          const channels = channelResult?.records || [];

          // Build map: deviceIdentification → channels[]
          const deviceChannelMap = new Map<string, any[]>();
          for (const ch of channels) {
            const devId = ch.deviceIdentification;
            if (!devId) continue;
            if (!deviceChannelMap.has(devId)) deviceChannelMap.set(devId, []);
            deviceChannelMap.get(devId)!.push(ch);
          }

          // Collect all groups (flatten tree)
          const allGroups = collectAllGroups(groups || []);

          if (allGroups.length > 0) {
            // Load device relations for all groups in parallel
            const relationsResults = await Promise.all(
              allGroups.map((g) => listByGroup(g.id).catch(() => [])),
            );

            const result: any[] = [];
            const assignedDeviceIds = new Set<string>();

            allGroups.forEach((group, idx) => {
              const relations = relationsResults[idx] || [];
              const deviceIds = [...new Set(relations.map((r: any) => r.deviceIdentification))];

              const groupChannels: any[] = [];
              for (const devId of deviceIds) {
                assignedDeviceIds.add(devId);
                groupChannels.push(...(deviceChannelMap.get(devId) || []));
              }

              if (groupChannels.length > 0) {
                result.push({
                  key: `group-${group.id}`,
                  title: group.groupName || group.id,
                  childCount: groupChannels.length,
                  children: groupChannels.map((ch: any) => ({
                    key: `ch-${ch.deviceIdentification}-${ch.channelIdentification}`,
                    title: ch.channelName || ch.channelIdentification,
                    online: isTruthyStatus(ch.onlineStatus),
                    sourceData: {
                      sourceType: 'channel',
                      deviceIdentification: ch.deviceIdentification,
                      channelIdentification: ch.channelIdentification,
                      channelName: ch.channelName,
                      hasAudio: ch.hasAudio,
                      ptzCapability: ch.ptzCapability,
                    } as StreamSource,
                  })),
                });
              }
            });

            // Add ungrouped channels
            const ungroupedChannels: any[] = [];
            for (const [devId, devChannels] of deviceChannelMap) {
              if (!assignedDeviceIds.has(devId)) {
                ungroupedChannels.push(...devChannels);
              }
            }
            if (ungroupedChannels.length > 0) {
              result.push({
                key: 'group-ungrouped',
                title: t('video.device.live.source.ungrouped'),
                childCount: ungroupedChannels.length,
                children: ungroupedChannels.map((ch: any) => ({
                  key: `ch-${ch.deviceIdentification}-${ch.channelIdentification}`,
                  title: ch.channelName || ch.channelIdentification,
                  online: isTruthyStatus(ch.onlineStatus),
                  sourceData: {
                    sourceType: 'channel',
                    deviceIdentification: ch.deviceIdentification,
                    channelIdentification: ch.channelIdentification,
                    channelName: ch.channelName,
                    hasAudio: ch.hasAudio,
                    ptzCapability: ch.ptzCapability,
                  } as StreamSource,
                })),
              });
            }

            groupTreeData.value = result;
          } else {
            // No groups configured - group channels by device
            const result: any[] = [];
            for (const [devId, chList] of deviceChannelMap) {
              result.push({
                key: `dev-${devId}`,
                title: devId,
                childCount: chList.length,
                children: chList.map((ch: any) => ({
                  key: `ch-${ch.deviceIdentification}-${ch.channelIdentification}`,
                  title: ch.channelName || ch.channelIdentification,
                  online: isTruthyStatus(ch.onlineStatus),
                  sourceData: {
                    sourceType: 'channel',
                    deviceIdentification: ch.deviceIdentification,
                    channelIdentification: ch.channelIdentification,
                    channelName: ch.channelName,
                    hasAudio: ch.hasAudio,
                    ptzCapability: ch.ptzCapability,
                  } as StreamSource,
                })),
              });
            }
            groupTreeData.value = result;
          }

          // Auto-expand first group
          if (groupTreeData.value.length > 0) {
            expandedGroups.value = new Set([groupTreeData.value[0].key]);
          }
          dataLoaded.value.group = true;
        } catch (e) {
          console.warn('Load group data error:', e);
          groupTreeData.value = [];
        } finally {
          loading.value = false;
        }
      }

      function collectAllGroups(groups: any[]): any[] {
        const result: any[] = [];
        for (const g of groups) {
          result.push(g);
          if (g.children?.length) {
            result.push(...collectAllGroups(g.children));
          }
        }
        return result;
      }

      const filteredGroupList = computed(() => {
        if (!searchText.value) return groupTreeData.value;
        const kw = searchText.value.toLowerCase();
        return groupTreeData.value
          .map((group) => {
            const filteredChildren = (group.children || []).filter(
              (ch: any) => (ch.title || '').toLowerCase().includes(kw),
            );
            const groupMatch = (group.title || '').toLowerCase().includes(kw);
            if (groupMatch || filteredChildren.length > 0) {
              return {
                ...group,
                children: groupMatch ? group.children : filteredChildren,
                childCount: groupMatch ? group.childCount : filteredChildren.length,
              };
            }
            return null;
          })
          .filter(Boolean);
      });

      // ==================== Administrative Region ====================
      const regionTreeData = ref<any[]>([]);

      function buildRegionTree() {
        loading.value = true;
        try {
          regionTreeData.value = (citiesData as any[]).map((province) => ({
            key: `region-${province.value}`,
            title: province.label,
            value: province.value,
            isLeaf: false,
            children: (province.children || []).map((city: any) => ({
              key: `region-${city.value}`,
              title: city.label,
              value: city.value,
              isLeaf: false,
              children: (city.children || []).map((district: any) => ({
                key: `region-${district.value}`,
                title: district.label,
                value: district.value,
                isLeaf: false,
                children: [],
              })),
            })),
          }));
          dataLoaded.value.region = true;
        } catch (e) {
          console.warn('Build region tree error:', e);
        } finally {
          loading.value = false;
        }
      }

      async function onRegionLoadData(treeNode: any) {
        if (treeNode.dataRef?.children?.length > 0) return;
        const regionCode = treeNode.dataRef?.value || treeNode.value;
        if (!regionCode) return;

        try {
          const result = await channelPage({
            model: { regionCode },
            size: 200,
            current: 1,
          } as any);
          const channels = result?.records || [];
          treeNode.dataRef.children = channels.map((ch: any) => ({
            key: `region-ch-${ch.deviceIdentification}-${ch.channelIdentification}`,
            title: ch.channelName || ch.channelIdentification,
            isLeaf: true,
            online: isTruthyStatus(ch.onlineStatus),
            sourceData: {
              sourceType: 'channel',
              deviceIdentification: ch.deviceIdentification,
              channelIdentification: ch.channelIdentification,
              channelName: ch.channelName,
              hasAudio: ch.hasAudio,
              ptzCapability: ch.ptzCapability,
            } as StreamSource,
          }));
          if (channels.length === 0) {
            treeNode.dataRef.isLeaf = true;
          }
        } catch (e) {
          console.warn('Load region channels error:', e);
          treeNode.dataRef.isLeaf = true;
        }
      }

      function onRegionSelect(_keys: any[], info: any) {
        const node = info?.node;
        if (node?.isLeaf && node?.sourceData) {
          handleSourceClick(node.sourceData);
        }
      }

      const filteredRegionTree = computed(() => {
        if (!searchText.value) return regionTreeData.value;
        return filterTree(regionTreeData.value, searchText.value.toLowerCase());
      });

      // ==================== Stream Proxy ====================
      const proxyList = ref<any[]>([]);

      async function loadProxyList() {
        loading.value = true;
        try {
          const result = await proxyPage({
            model: {},
            size: 500,
            current: 1,
          } as any);
          proxyList.value = result?.records || [];
          dataLoaded.value.proxy = true;
        } catch (e) {
          console.warn('Load proxy list error:', e);
          proxyList.value = [];
        } finally {
          loading.value = false;
        }
      }

      const filteredProxyList = computed(() => {
        if (!searchText.value) return proxyList.value;
        const kw = searchText.value.toLowerCase();
        return proxyList.value.filter(
          (p) =>
            (p.proxyName || '').toLowerCase().includes(kw) ||
            (p.streamIdentification || '').toLowerCase().includes(kw),
        );
      });

      function makeProxySource(item: any): StreamSource {
        return {
          sourceType: 'proxy',
          proxyId: item.id,
          proxyName: item.proxyName,
          streamIdentification: item.streamIdentification,
          appId: item.appId,
          id: item.id,
        };
      }

      // ==================== Stream Push ====================
      const pushList = ref<any[]>([]);

      async function loadPushList() {
        loading.value = true;
        try {
          const result = await pushPage({
            model: {},
            size: 500,
            current: 1,
          } as any);
          pushList.value = result?.records || [];
          dataLoaded.value.push = true;
        } catch (e) {
          console.warn('Load push list error:', e);
          pushList.value = [];
        } finally {
          loading.value = false;
        }
      }

      const filteredPushList = computed(() => {
        if (!searchText.value) return pushList.value;
        const kw = searchText.value.toLowerCase();
        return pushList.value.filter(
          (p) => (p.streamIdentification || '').toLowerCase().includes(kw),
        );
      });

      function makePushSource(item: any): StreamSource {
        return {
          sourceType: 'push',
          pushId: item.id,
          streamIdentification: item.streamIdentification,
          appId: item.appId,
          id: item.id,
        };
      }

      // ==================== Common ====================
      const currentTypeLabel = computed(() => {
        const opt = sourceTypeOptions.value.find((o) => o.value === sourceType.value);
        return opt?.label || '';
      });

      const currentCount = computed(() => {
        switch (sourceType.value) {
          case 'group': {
            let count = 0;
            for (const g of groupTreeData.value) {
              count += g.childCount || 0;
            }
            return count;
          }
          case 'proxy':
            return proxyList.value.length;
          case 'push':
            return pushList.value.length;
          case 'region':
            return regionTreeData.value.length;
          default:
            return 0;
        }
      });

      function onDragStart(e: DragEvent, source: StreamSource | undefined) {
        if (!source || !e.dataTransfer) return;
        e.dataTransfer.effectAllowed = 'copy';
        e.dataTransfer.setData('application/json', JSON.stringify(source));
        e.dataTransfer.setData(
          'text/plain',
          source.channelName || source.proxyName || source.streamIdentification || 'stream',
        );
      }

      function filterTree(nodes: any[], keyword: string): any[] {
        const result: any[] = [];
        for (const node of nodes) {
          const titleMatch = (node.title || '').toLowerCase().includes(keyword);
          const filteredChildren = node.children ? filterTree(node.children, keyword) : [];
          if (titleMatch || filteredChildren.length > 0) {
            result.push({
              ...node,
              children: titleMatch ? node.children : filteredChildren,
            });
          }
        }
        return result;
      }

      /** Load data for the given type (with cache) */
      function loadTypeData(type: SourceFilterType, force = false) {
        if (dataLoaded.value[type] && !force) return;
        switch (type) {
          case 'group':
            loadGroupData();
            break;
          case 'region':
            buildRegionTree();
            break;
          case 'proxy':
            loadProxyList();
            break;
          case 'push':
            loadPushList();
            break;
        }
      }

      function refreshCurrent() {
        loadTypeData(sourceType.value, true);
      }

      // Watch type change and load data
      watch(sourceType, (newType) => {
        searchText.value = '';
        loadTypeData(newType);
      });

      // Load default type on mount
      onMounted(() => {
        loadTypeData(sourceType.value);
      });

      return {
        t,
        simpleImage,
        searchText,
        searchPlaceholder,
        sourceType,
        sourceTypeOptions,
        loading,
        currentTypeLabel,
        currentCount,
        // Group
        filteredGroupList,
        expandedGroups,
        toggleGroup,
        // Region
        filteredRegionTree,
        onRegionLoadData,
        onRegionSelect,
        // Proxy
        filteredProxyList,
        makeProxySource,
        // Push
        filteredPushList,
        makePushSource,
        // Common
        onDragStart,
        handleSourceClick,
        refreshCurrent,
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

  .source-tree {
    display: flex;
    flex-direction: column;
    height: 100%;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

    &__header {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 12px;
      flex-shrink: 0;
      position: relative;
      overflow: visible;
      z-index: 10;
    }

    &__type-select {
      width: 100%;

      :deep(.ant-select-selector) {
        border-radius: 8px;
        background: #f0f4ff;
        border-color: transparent;
        font-weight: 500;
        color: @primary;

        &:hover {
          border-color: @primary;
        }
      }

      :deep(.ant-select-focused .ant-select-selector) {
        border-color: @primary;
        background: #fff;
      }
    }

    &__search {
      width: 100%;

      :deep(.ant-input-affix-wrapper) {
        border-radius: 8px;
        background: #f6f8fb;
        border-color: transparent;

        &:hover,
        &:focus,
        &-focused {
          background: #fff;
          border-color: @primary;
        }

        .ant-input {
          background: transparent;
        }
      }
    }

    &__content {
      flex: 1;
      overflow-y: auto;
      padding: 0 4px 8px;

      &::-webkit-scrollbar {
        width: 4px;
      }

      &::-webkit-scrollbar-thumb {
        background: #d4dbe4;
        border-radius: 4px;
      }
    }

    &__group-header {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 10px;
      font-size: 13px;
      font-weight: 600;
      color: #2a3547;
      cursor: pointer;
      border-radius: 8px;
      transition: background 0.15s;
      user-select: none;

      &:hover {
        background: #f6f8fb;
      }
    }

    &__group-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__group-arrow {
      color: #8c97a5;
      flex-shrink: 0;
    }

    &__region-node {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
    }

    &__node-count {
      font-size: 11px;
      color: #8c97a5;
      background: #f0f2f5;
      padding: 0 6px;
      border-radius: 10px;
      flex-shrink: 0;
    }

    &__node-title--offline {
      color: #8c97a5;
    }

    &__node-status {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      flex-shrink: 0;

      &--online {
        background: @success;
        box-shadow: 0 0 4px fade(@success, 50%);
      }

      &--offline {
        background: #d4dbe4;
      }
    }

    &__list {
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding: 0 4px;
    }

    &__list-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 7px 10px;
      border-radius: 8px;
      cursor: grab;
      transition: all 0.15s;
      border: 1px solid transparent;
      user-select: none;

      &:hover {
        background: #f6f8fb;
        border-color: #e5eaef;
      }

      &:active {
        cursor: grabbing;
        opacity: 0.7;
      }

      &--offline {
        opacity: 0.7;
      }

      &-left-icon {
        flex-shrink: 0;
        pointer-events: none;
      }

      &-icon {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        color: #fff;
        pointer-events: none;

        &.proxy {
          background: linear-gradient(135deg, @purple, fade(@purple, 70%));
        }

        &.push {
          background: linear-gradient(135deg, @warning, fade(@warning, 70%));
        }
      }

      &-info {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        gap: 2px;
        pointer-events: none;
      }

      &-name {
        font-size: 13px;
        font-weight: 500;
        color: #2a3547;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        pointer-events: none;
      }

      &-desc {
        font-size: 11px;
        color: #8c97a5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        pointer-events: none;
      }

      &-status {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        flex-shrink: 0;
        pointer-events: none;

        &.online {
          background: @success;
          box-shadow: 0 0 4px fade(@success, 50%);
        }

        &.offline {
          background: #d4dbe4;
        }
      }
    }

    &__footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 14px;
      border-top: 1px solid #f0f2f5;
      font-size: 12px;
      color: #8c97a5;
    }
  }
</style>
