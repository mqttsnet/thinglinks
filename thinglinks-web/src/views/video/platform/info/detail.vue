<template>
  <PageWrapper contentFullHeight>
    <!-- 顶部概览卡片 -->
    <div class="platform-detail">
      <Card :bordered="false" class="header-card">
        <div class="header-content">
          <div class="header-left">
            <div class="platform-icon">
              <ShareAltOutlined style="font-size: 32px; color: #fff" />
            </div>
            <div class="platform-meta">
              <div class="platform-name">
                <span>{{ platformDetail.name }}</span>
                <Tag :color="isTruthyStatus(platformDetail.onlineStatus) ? 'success' : 'error'" class="status-tag">
                  {{ isTruthyStatus(platformDetail.onlineStatus) ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                </Tag>
                <Tag :color="isTruthyStatus(platformDetail.enable) ? 'processing' : 'default'" class="status-tag">
                  {{
                    isTruthyStatus(platformDetail.enable)
                      ? t('video.platform.info.enablePlatform')
                      : t('video.platform.info.disablePlatform')
                  }}
                </Tag>
              </div>
              <div class="platform-summary">
                <span class="summary-item">
                  <ApartmentOutlined />
                  {{ getDictLabel(DictEnum.VIDEO_PLATFORM_CASCADE_TYPE, platformDetail?.cascadeType) }}
                </span>
                <a-divider type="vertical" />
                <span class="summary-item">
                  <ApiOutlined />
                  {{ getDictLabel(DictEnum.VIDEO_DEVICE_TRANSPORT, platformDetail?.transport) }}
                </span>
                <a-divider type="vertical" />
                <span class="summary-item">
                  <TagOutlined />
                  {{ getDictLabel(DictEnum.VIDEO_PLATFORM_GB_VERSION, platformDetail?.gbVersion) }}
                </span>
                <a-divider type="vertical" />
                <span class="summary-item">
                  <ClockCircleOutlined />
                  {{ platformDetail.createdTime }}
                </span>
              </div>
            </div>
          </div>
          <div class="header-actions">
            <a-button
              :type="platformDetail.enable ? 'default' : 'primary'"
              @click="handleToggleEnable"
              v-hasAnyPermission="['video:platform:info:enable']"
            >
              <template #icon><PoweroffOutlined /></template>
              {{
                platformDetail.enable
                  ? t('video.platform.info.disablePlatform')
                  : t('video.platform.info.enablePlatform')
              }}
            </a-button>
            <a-button
              type="primary"
              @click="handleEdit"
              v-hasAnyPermission="['video:platform:info:edit']"
            >
              <template #icon><EditOutlined /></template>
              {{ t('common.title.edit') }}
            </a-button>
          </div>
        </div>
      </Card>

      <!-- Tab 区域: 基本信息 / 平台目录 / 平台通道 -->
      <Card :bordered="false" class="tab-card">
        <a-tabs v-model:activeKey="activeTab">
          <!-- 基本信息 Tab -->
          <a-tab-pane key="info" :tab="t('video.platform.info.basicInfo')">
            <!-- 连接信息 - 双栏 -->
            <a-row :gutter="16" class="section-row">
              <a-col :span="12">
                <Card :bordered="false" class="section-card">
                  <template #title>
                    <div class="section-title">
                      <CloudServerOutlined />
                      <span>{{ t('video.platform.info.serverGbId') }}</span>
                    </div>
                  </template>
                  <a-descriptions :column="1" bordered size="small">
                    <a-descriptions-item :label="t('video.platform.info.serverGbId')">
                      <span class="mono-text">{{ platformDetail.serverGbId }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.serverGbDomain')">
                      {{ platformDetail.serverGbDomain }}
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.serverIp')">
                      <span class="mono-text">{{ platformDetail.serverIp }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.serverPort')">
                      <span class="mono-text">{{ platformDetail.serverPort }}</span>
                    </a-descriptions-item>
                  </a-descriptions>
                </Card>
              </a-col>
              <a-col :span="12">
                <Card :bordered="false" class="section-card">
                  <template #title>
                    <div class="section-title">
                      <DesktopOutlined />
                      <span>{{ t('video.platform.info.deviceGbId') }}</span>
                    </div>
                  </template>
                  <a-descriptions :column="1" bordered size="small">
                    <a-descriptions-item :label="t('video.platform.info.deviceGbId')">
                      <span class="mono-text">{{ platformDetail.deviceGbId }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.deviceIp')">
                      <span class="mono-text">{{ platformDetail.deviceIp }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.devicePort')">
                      <span class="mono-text">{{ platformDetail.devicePort }}</span>
                    </a-descriptions-item>
                    <a-descriptions-item :label="t('video.platform.info.serverId')">
                      {{ platformDetail.serverId }}
                    </a-descriptions-item>
                  </a-descriptions>
                </Card>
              </a-col>
            </a-row>

            <!-- SIP 配置 -->
            <Card :bordered="false" class="section-card">
              <template #title>
                <div class="section-title">
                  <PhoneOutlined />
                  <span>{{ t('video.platform.info.sipConfig') }}</span>
                </div>
              </template>
              <a-descriptions :column="3" bordered size="small">
                <a-descriptions-item :label="t('video.platform.info.username')">
                  {{ platformDetail.username }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.characterSet')">
                  {{ platformDetail.characterSet }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.registerWay')">
                  {{ getDictLabel(DictEnum.VIDEO_GB28181_SIP_REGISTER_WAY, platformDetail?.registerWay) }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.secrecy')">
                  {{ getDictLabel(DictEnum.VIDEO_DEVICE_SECRECY, platformDetail?.secrecy) }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.expires')">
                  {{ platformDetail.expires }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.keepTimeout')">
                  {{ platformDetail.keepTimeout }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.sipIp')">
                  <span class="mono-text">{{ platformDetail.sipIp }}</span>
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.sipPort')">
                  <span class="mono-text">{{ platformDetail.sipPort }}</span>
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.registerExpires')">
                  {{ platformDetail.registerExpires }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.keepaliveInterval')">
                  {{ platformDetail.keepaliveInterval }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.keepaliveTimeoutCount')">
                  {{ platformDetail.keepaliveTimeoutCount }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.lastRegisterTime')">
                  {{ platformDetail.lastRegisterTime }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.lastKeepaliveTime')">
                  {{ platformDetail.lastKeepaliveTime }}
                </a-descriptions-item>
              </a-descriptions>
            </Card>

            <!-- 订阅与功能 - 双栏 -->
            <a-row :gutter="16" class="section-row">
              <a-col :span="12">
                <Card :bordered="false" class="section-card">
                  <template #title>
                    <div class="section-title">
                      <BellOutlined />
                      <span>{{ t('video.platform.info.subscribeConfig') }}</span>
                    </div>
                  </template>
                  <div class="switch-grid">
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.catalogSubscribe') }}</span>
                      <Tag :color="platformDetail.catalogSubscribe ? 'success' : 'default'">
                        {{ platformDetail.catalogSubscribe ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.alarmSubscribe') }}</span>
                      <Tag :color="platformDetail.alarmSubscribe ? 'success' : 'default'">
                        {{ platformDetail.alarmSubscribe ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.mobilePositionSubscribe') }}</span>
                      <Tag :color="platformDetail.mobilePositionSubscribe ? 'success' : 'default'">
                        {{ platformDetail.mobilePositionSubscribe ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                  </div>
                </Card>
              </a-col>
              <a-col :span="12">
                <Card :bordered="false" class="section-card">
                  <template #title>
                    <div class="section-title">
                      <SettingOutlined />
                      <span>{{ t('video.platform.info.advancedConfig') }}</span>
                    </div>
                  </template>
                  <div class="switch-grid">
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.ptz') }}</span>
                      <Tag :color="platformDetail.ptz ? 'success' : 'default'">
                        {{ platformDetail.ptz ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.rtcp') }}</span>
                      <Tag :color="platformDetail.rtcp ? 'success' : 'default'">
                        {{ platformDetail.rtcp ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.asMessageChannel') }}</span>
                      <Tag :color="platformDetail.asMessageChannel ? 'success' : 'default'">
                        {{ platformDetail.asMessageChannel ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.autoPushChannel') }}</span>
                      <Tag :color="platformDetail.autoPushChannel ? 'success' : 'default'">
                        {{ platformDetail.autoPushChannel ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                    <div class="switch-item">
                      <span>{{ t('video.platform.info.startOfflinePush') }}</span>
                      <Tag :color="platformDetail.startOfflinePush ? 'success' : 'default'">
                        {{ platformDetail.startOfflinePush ? t('thinglinks.common.yes') : t('thinglinks.common.no') }}
                      </Tag>
                    </div>
                  </div>
                </Card>
              </a-col>
            </a-row>

            <!-- 其他信息 -->
            <Card :bordered="false" class="section-card">
              <template #title>
                <div class="section-title">
                  <InfoCircleOutlined />
                  <span>{{ t('video.platform.info.basicInfo') }}</span>
                </div>
              </template>
              <a-descriptions :column="3" bordered size="small">
                <a-descriptions-item :label="t('video.platform.info.civilCode')">
                  {{ platformDetail.civilCode }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.manufacturer')">
                  {{ platformDetail.manufacturer }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.model')">
                  {{ platformDetail.model }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.address')">
                  {{ platformDetail.address }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.sendStreamIp')">
                  <span class="mono-text">{{ platformDetail.sendStreamIp }}</span>
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.hookUrlPrefix')">
                  <span class="mono-text">{{ platformDetail.hookUrlPrefix }}</span>
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.cascadeSdpIp')">
                  <span class="mono-text">{{ platformDetail.cascadeSdpIp }}</span>
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.serviceInstanceId')">
                  {{ platformDetail.serviceInstanceId }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('video.platform.info.createdOrgId')">
                  {{ echoMapText(platformDetail, 'createdOrgId') }}
                </a-descriptions-item>
                <a-descriptions-item :label="t('thinglinks.common.createdTime')">
                  {{ platformDetail.createdTime }}
                </a-descriptions-item>
              </a-descriptions>
            </Card>
          </a-tab-pane>

          <!-- 平台目录 Tab -->
          <a-tab-pane key="catalog" :tab="t('video.platform.catalog.table.title')">
            <div class="catalog-container">
              <!-- 目录树 -->
              <div class="catalog-tree-panel">
                <div class="panel-header">
                  <span class="panel-title">{{ t('video.platform.catalog.table.title') }}</span>
                  <a-button
                    size="small"
                    type="primary"
                    @click="handleAddCatalog()"
                    v-hasAnyPermission="['video:platform:catalog:add']"
                  >
                    <template #icon><PlusOutlined /></template>
                  </a-button>
                </div>
                <a-spin :spinning="catalogLoading">
                  <a-tree
                    v-if="catalogTreeData.length > 0"
                    :tree-data="catalogTreeData"
                    :field-names="{ title: 'name', key: 'id', children: 'children' }"
                    default-expand-all
                    :selectedKeys="selectedCatalogKeys"
                    @select="onCatalogSelect"
                  >
                    <template #title="{ name, id: nodeId }">
                      <div class="catalog-tree-node">
                        <FolderOutlined />
                        <span class="catalog-name">{{ name }}</span>
                        <div class="catalog-actions" @click.stop>
                          <a-button
                            size="small"
                            type="link"
                            @click="handleAddCatalog(nodeId)"
                            v-hasAnyPermission="['video:platform:catalog:add']"
                          >
                            <PlusOutlined />
                          </a-button>
                          <a-button
                            size="small"
                            type="link"
                            @click="handleEditCatalog(nodeId)"
                            v-hasAnyPermission="['video:platform:catalog:edit']"
                          >
                            <EditOutlined />
                          </a-button>
                          <a-button
                            size="small"
                            type="link"
                            danger
                            @click="handleDeleteCatalog(nodeId)"
                            v-hasAnyPermission="['video:platform:catalog:delete']"
                          >
                            <DeleteOutlined />
                          </a-button>
                        </div>
                      </div>
                    </template>
                  </a-tree>
                  <a-empty v-else :description="false" />
                </a-spin>
              </div>
            </div>

            <!-- 目录编辑弹窗 -->
            <BasicModal
              @register="registerCatalogModal"
              :title="catalogModalTitle"
              @ok="handleCatalogSubmit"
              :destroyOnClose="true"
              :width="500"
            >
              <BasicForm @register="registerCatalogForm" />
            </BasicModal>
          </a-tab-pane>

          <!-- 平台通道 Tab -->
          <a-tab-pane key="channel" :tab="t('video.platform.channel.table.title')">
            <div class="channel-toolbar">
              <a-button
                type="primary"
                @click="handleBindChannel"
                v-hasAnyPermission="['video:platform:channel:bind']"
              >
                <template #icon><LinkOutlined /></template>
                {{ t('video.platform.channel.bindChannel') }}
              </a-button>
              <a-button
                danger
                :disabled="selectedChannelIds.length === 0"
                @click="handleUnbindChannel"
                v-hasAnyPermission="['video:platform:channel:unbind']"
              >
                <template #icon><DisconnectOutlined /></template>
                {{ t('video.platform.channel.unbindChannel') }}
              </a-button>
            </div>
            <BasicTable @register="registerChannelTable" />

            <!-- 绑定通道弹窗 -->
            <BasicModal
              @register="registerBindModal"
              :title="t('video.platform.channel.bindChannel')"
              @ok="handleBindSubmit"
              :destroyOnClose="true"
              :width="900"
            >
              <BasicTable @register="registerBindTable" />
            </BasicModal>
          </a-tab-pane>
        </a-tabs>
      </Card>
    </div>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { ref, reactive, onMounted, computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { detail, setEnable } from '/@/api/video/platform/info';
  import {
    listByPlatform as listCatalogByPlatform,
    save as saveCatalog,
    update as updateCatalog,
    remove as removeCatalog,
  } from '/@/api/video/platform/catalog';
  import {
    page as channelPage,
    bind as bindChannel,
    unbind as unbindChannel,
  } from '/@/api/video/platform/channel';
  import { page as deviceChannelPage } from '/@/api/video/device/channel';
  import type { VideoPlatformResultVO } from '/@/api/video/platform/model/infoModel';
  import type { VideoPlatformCatalogResultVO } from '/@/api/video/platform/model/catalogModel';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModal } from '/@/components/Modal';
  import {
    EditOutlined,
    PoweroffOutlined,
    ShareAltOutlined,
    CloudServerOutlined,
    DesktopOutlined,
    PhoneOutlined,
    BellOutlined,
    SettingOutlined,
    InfoCircleOutlined,
    ApartmentOutlined,
    ApiOutlined,
    TagOutlined,
    ClockCircleOutlined,
    PlusOutlined,
    DeleteOutlined,
    FolderOutlined,
    LinkOutlined,
    DisconnectOutlined,
  } from '@ant-design/icons-vue';
  import { useDict } from '/@/components/Dict';
  import EditModal from './Edit.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ActionEnum, DictEnum } from '/@/enums/commonEnum';
  import { Tag, Card } from 'ant-design-vue';
  import { handleFetchParams, dictComponentProps, isTruthyStatus } from '/@/utils/thinglinks/common';
  import citiesGd from '/@/utils/thinglinks/citiesGd.json';
  import { echoMapText } from '/@/utils/echo';

  const { getDictLabel } = useDict();
  const { t } = useI18n();
  const { currentRoute } = useRouter();
  const { createMessage, createConfirm } = useMessage();

  let platformDetail = reactive<Partial<VideoPlatformResultVO>>({});
  const [registerModal, { openModal }] = useModal();
  let id = ref('');
  const activeTab = ref('info');

  // ========== Catalog ==========
  const catalogLoading = ref(false);
  const catalogTreeData = ref<any[]>([]);
  const selectedCatalogKeys = ref<string[]>([]);
  const catalogModalTitle = ref('');
  const editingCatalogId = ref<string | null>(null);
  const editingCatalogParentId = ref<string | null>(null);

  const [registerCatalogModal, { openModal: openCatalogModal, closeModal: closeCatalogModal }] = useModal();
  const [registerCatalogForm, { setFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: [
      { field: 'id', label: 'ID', component: 'Input', show: false },
      {
        field: 'name',
        label: t('video.platform.catalog.name'),
        component: 'Input',
        required: true,
      },
      {
        field: 'gbId',
        label: t('video.platform.catalog.gbId'),
        component: 'Input',
      },
      {
        field: 'catalogType',
        label: t('video.platform.catalog.catalogType'),
        component: 'ApiSelect',
        defaultValue: 0,
        componentProps: {
          ...dictComponentProps(DictEnum.VIDEO_PLATFORM_CATALOG_TYPE),
        },
      },
      {
        field: 'civilCodeArea',
        label: t('video.platform.catalog.civilCode'),
        component: 'Cascader',
        componentProps: ({ formModel }) => {
          return {
            options: citiesGd,
            changeOnSelect: true,
            showSearch: true,
            placeholder: t('video.platform.catalog.civilCode'),
            onChange: (value: string[]) => {
              if (value && value.length > 0) {
                formModel.civilCode = value[value.length - 1];
              } else {
                formModel.civilCode = '';
              }
            },
          };
        },
      },
      {
        field: 'civilCode',
        label: '',
        component: 'Input',
        show: false,
      },
      {
        field: 'sortOrder',
        label: t('video.platform.catalog.sortOrder'),
        component: 'InputNumber',
        defaultValue: 0,
      },
    ],
    showActionButtonGroup: false,
  });

  function buildCatalogTree(list: VideoPlatformCatalogResultVO[]): any[] {
    const map = new Map<string, any>();
    const roots: any[] = [];
    for (const item of list) {
      map.set(item.id, { ...item, children: [] });
    }
    for (const item of list) {
      const node = map.get(item.id);
      if (item.parentId && map.has(item.parentId)) {
        map.get(item.parentId).children.push(node);
      } else {
        roots.push(node);
      }
    }
    return roots;
  }

  async function loadCatalogs() {
    if (!id.value) return;
    catalogLoading.value = true;
    try {
      const list = await listCatalogByPlatform(id.value);
      catalogTreeData.value = buildCatalogTree(list || []);
    } finally {
      catalogLoading.value = false;
    }
  }

  function onCatalogSelect(keys: string[]) {
    selectedCatalogKeys.value = keys;
  }

  function handleAddCatalog(parentId?: string) {
    catalogModalTitle.value = t('video.platform.catalog.addCatalog');
    editingCatalogId.value = null;
    editingCatalogParentId.value = parentId || null;
    resetFields();
    openCatalogModal(true);
  }

  /** 根据 civilCode 反查省市区级联路径 */
  function resolveCivilCodePath(code: string): string[] {
    if (!code) return [];
    for (const province of citiesGd) {
      if (province.value === code) return [code];
      if (province.children) {
        for (const city of province.children) {
          if (city.value === code) return [province.value, code];
          if (city.children) {
            for (const district of city.children) {
              if (district.value === code) return [province.value, city.value, code];
            }
          }
        }
      }
    }
    return [];
  }

  function handleEditCatalog(catalogId: string) {
    catalogModalTitle.value = t('video.platform.catalog.editCatalog');
    editingCatalogId.value = catalogId;
    const catalog = findCatalogById(catalogTreeData.value, catalogId);
    if (catalog) {
      resetFields();
      const civilCodePath = resolveCivilCodePath(catalog.civilCode);
      setFieldsValue({
        id: catalog.id,
        name: catalog.name,
        gbId: catalog.gbId,
        catalogType: catalog.catalogType,
        civilCode: catalog.civilCode,
        civilCodeArea: civilCodePath.length > 0 ? civilCodePath : undefined,
        sortOrder: catalog.sortOrder,
      });
    }
    openCatalogModal(true);
  }

  function findCatalogById(tree: any[], catalogId: string): any | null {
    for (const node of tree) {
      if (node.id === catalogId) return node;
      if (node.children?.length) {
        const found = findCatalogById(node.children, catalogId);
        if (found) return found;
      }
    }
    return null;
  }

  function handleDeleteCatalog(catalogId: string) {
    createConfirm({
      iconType: 'warning',
      title: t('video.platform.catalog.deleteCatalog'),
      content: t('video.platform.catalog.confirmDelete'),
      onOk: async () => {
        try {
          await removeCatalog([catalogId]);
          createMessage.success(t('video.platform.catalog.deleteSuccess'));
          loadCatalogs();
        } catch (e: any) {
          createMessage.error(e?.message || 'Delete failed');
        }
      },
    });
  }

  async function handleCatalogSubmit() {
    try {
      const values = await validate();
      if (editingCatalogId.value) {
        await updateCatalog({
          ...values,
          id: editingCatalogId.value,
          platformId: id.value,
          parentId: editingCatalogParentId.value || undefined,
        });
      } else {
        await saveCatalog({
          ...values,
          platformId: id.value,
          parentId: editingCatalogParentId.value || undefined,
        });
      }
      createMessage.success(t('video.platform.catalog.saveCatalogSuccess'));
      closeCatalogModal();
      loadCatalogs();
    } catch (e: any) {
      createMessage.error(e?.message || 'Save failed');
    }
  }

  // ========== Channel ==========
  const selectedChannelIds = ref<string[]>([]);

  const [registerChannelTable, { reload: reloadChannels }] = useTable({
    api: channelPage,
    columns: [
      { title: t('video.platform.channel.deviceIdentification'), dataIndex: 'deviceIdentification', width: 200 },
      { title: t('video.platform.channel.channelIdentification'), dataIndex: 'channelIdentification', width: 200 },
      { title: t('video.platform.channel.customName'), dataIndex: 'customName', width: 180 },
      { title: t('video.platform.channel.customGbId'), dataIndex: 'customGbId', width: 200 },
      { title: t('thinglinks.common.createdTime'), dataIndex: 'createdTime', width: 180, sorter: true },
    ],
    beforeFetch: (params) => {
      return handleFetchParams({ ...params, model: { ...params.model, platformId: id.value } });
    },
    useSearchForm: false,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    rowSelection: {
      type: 'checkbox',
      onChange: (_keys: string[]) => {
        selectedChannelIds.value = _keys;
      },
    },
    immediate: false,
  });

  function handleUnbindChannel() {
    if (selectedChannelIds.value.length === 0) {
      createMessage.warning(t('video.platform.channel.noChannelsSelected'));
      return;
    }
    createConfirm({
      iconType: 'warning',
      title: t('video.platform.channel.unbindChannel'),
      content: t('video.platform.channel.confirmUnbind'),
      onOk: async () => {
        try {
          await unbindChannel(id.value, selectedChannelIds.value);
          createMessage.success(t('video.platform.channel.unbindSuccess'));
          selectedChannelIds.value = [];
          reloadChannels();
        } catch (e: any) {
          createMessage.error(e?.message || 'Unbind failed');
        }
      },
    });
  }

  // ========== Bind Modal ==========
  const selectedBindIds = ref<string[]>([]);

  const [registerBindModal, { openModal: openBindModal, closeModal: closeBindModal }] = useModal();
  const [registerBindTable] = useTable({
    api: deviceChannelPage,
    columns: [
      { title: t('video.device.channel.deviceIdentification'), dataIndex: 'deviceIdentification', width: 200 },
      { title: t('video.device.channel.channelIdentification'), dataIndex: 'channelIdentification', width: 200 },
      { title: t('video.device.channel.channelName'), dataIndex: 'channelName', width: 180 },
      { title: t('video.device.channel.onlineStatus'), dataIndex: 'onlineStatus', width: 100 },
    ],
    formConfig: {
      labelWidth: 100,
      schemas: [
        {
          label: t('video.device.channel.deviceIdentification'),
          field: 'deviceIdentification',
          component: 'Input',
          colProps: { span: 8 },
        },
        {
          label: t('video.device.channel.channelName'),
          field: 'channelName',
          component: 'Input',
          colProps: { span: 8 },
        },
      ],
      autoSubmitOnEnter: true,
    },
    beforeFetch: handleFetchParams,
    useSearchForm: true,
    showTableSetting: false,
    bordered: true,
    rowKey: 'id',
    rowSelection: {
      type: 'checkbox',
      onChange: (_keys: string[]) => {
        selectedBindIds.value = _keys;
      },
    },
  });

  function handleBindChannel() {
    selectedBindIds.value = [];
    openBindModal(true);
  }

  async function handleBindSubmit() {
    if (selectedBindIds.value.length === 0) {
      createMessage.warning(t('video.platform.channel.noChannelsSelected'));
      return;
    }
    try {
      await bindChannel({
        platformId: id.value,
        channelIds: selectedBindIds.value,
        catalogId: selectedCatalogKeys.value[0] || undefined,
      });
      createMessage.success(t('video.platform.channel.bindSuccess'));
      closeBindModal();
      reloadChannels();
    } catch (e: any) {
      createMessage.error(e?.message || 'Bind failed');
    }
  }

  // ========== Common ==========
  onMounted(() => {
    const { params } = currentRoute.value;
    id.value = params.id as string;
    load();
  });

  const load = async () => {
    const res = await detail(id.value);
    Object.assign(platformDetail, res);
    loadCatalogs();
    reloadChannels();
  };

  function handleEdit(e: Event) {
    e?.stopPropagation();
    openModal(true, {
      record: platformDetail,
      type: ActionEnum.EDIT,
    });
  }

  async function handleToggleEnable() {
    createConfirm({
      iconType: 'warning',
      title: platformDetail.enable
        ? t('video.platform.info.disablePlatform')
        : t('video.platform.info.enablePlatform'),
      content: platformDetail.enable
        ? t('video.platform.info.confirmDisable')
        : t('video.platform.info.confirmEnable'),
      onOk: async () => {
        await setEnable(id.value, !platformDetail.enable);
        createMessage.success(t('common.tips.editSuccess'));
        load();
      },
    });
  }

  function handleSuccess() {
    load();
  }
</script>
<style lang="less" scoped>
  .platform-detail {
    .header-card {
      margin-bottom: 16px;

      .header-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .header-left {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .platform-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        background: linear-gradient(135deg, #1890ff, #36cfc9);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .platform-meta {
        .platform-name {
          font-size: 20px;
          font-weight: 600;
          color: #1f2937;
          margin-bottom: 8px;
          display: flex;
          align-items: center;
          gap: 8px;

          .status-tag {
            font-size: 12px;
          }
        }

        .platform-summary {
          display: flex;
          align-items: center;
          color: #6b7280;
          font-size: 13px;

          .summary-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      .header-actions {
        display: flex;
        gap: 8px;
        flex-shrink: 0;
      }
    }

    .tab-card {
      margin-bottom: 16px;
    }

    .section-row {
      margin-bottom: 16px;
    }

    .section-card {
      margin-bottom: 16px;

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 15px;
        font-weight: 500;
      }
    }

    .switch-grid {
      .switch-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        border-radius: 6px;
        transition: background-color 0.2s;

        &:hover {
          background-color: #f9fafb;
        }

        & + .switch-item {
          border-top: 1px solid #f0f0f0;
        }

        span {
          color: #374151;
          font-size: 14px;
        }
      }
    }

    .mono-text {
      font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
      color: #1890ff;
    }

    // Catalog styles
    .catalog-container {
      .catalog-tree-panel {
        .panel-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 12px;

          .panel-title {
            font-weight: 600;
            font-size: 14px;
          }
        }

        .catalog-tree-node {
          display: inline-flex;
          align-items: center;
          gap: 6px;
          width: 100%;

          .catalog-name {
            flex: 1;
          }

          .catalog-actions {
            display: none;
            margin-left: 8px;
          }

          &:hover .catalog-actions {
            display: inline-flex;
          }
        }
      }
    }

    // Channel styles
    .channel-toolbar {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
    }
  }
</style>
