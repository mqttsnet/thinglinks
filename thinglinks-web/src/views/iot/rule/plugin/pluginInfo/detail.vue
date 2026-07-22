<template>
  <PageWrapper contentFullHeight v-loading="getLoading" :loading-tip="t('common.loadingText')">
    <div class="detail-info">
      <a-card title="" :bordered="false">
        <div class="device_title">
          <span>{{ pluginInfoDetail.pluginName }}</span>
          <a-button type="primary" danger @click="handlePreload">
            {{ t('iot.rule.plugin.pluginInfo.updateButton') }}
          </a-button>
        </div>
        <div class="base_data">
          <div class="item">
            <span>{{ t('iot.rule.plugin.pluginInfo.type') }}：</span>
            <span>{{ getDictLabel('RULE_PLUGIN_INFO_TYPE', pluginInfoDetail?.type, '') }}</span>
          </div>
          <div class="item">
            <span>{{ t('iot.rule.plugin.pluginInfo.pluginIdentification') }}：</span>
            <span>{{ pluginInfoDetail.pluginIdentification }}</span>
          </div>
          <div class="item">
            <span>{{ t('iot.rule.plugin.pluginInfo.status') }}：</span>
            <span class="green" v-if="pluginInfoDetail?.status == 0">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
            <span class="green" v-if="pluginInfoDetail?.status == 1">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
            <span class="green" v-if="pluginInfoDetail?.status == 3">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
            <span class="orange" v-else-if="pluginInfoDetail?.status == 5">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
            <span class="red" v-else-if="pluginInfoDetail?.status == 2">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
            <span class="red" v-else-if="pluginInfoDetail?.status == 4">{{
              getDictLabel('RULE_PLUGIN_INFO_STATUS', pluginInfoDetail?.status, '')
            }}</span>
          </div>
        </div>
      </a-card>
    </div>
    <div class="detail-info">
      <a-card :title="t('iot.rule.plugin.pluginInfo.basicInfo')" :bordered="false">
        <p class="unfold" @click="descShow = !descShow">
          <DownOutlined v-if="!descShow" />
          <UpOutlined v-if="descShow" />
        </p>
        <a-descriptions bordered v-if="descShow">
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.version')"
          >
            {{ pluginInfoDetail?.version }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.description')"
          >
            {{ pluginInfoDetail?.description }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.fileSize')"
          >
            {{ pluginInfoDetail?.fileSize }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.level')"
          >
            {{ getDictLabel('RULE_PLUGIN_INFO_LEVEL', pluginInfoDetail?.level, '') }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.runMode')"
          >
            {{ getDictLabel('RULE_PLUGIN_INFO_RUN_MODE', pluginInfoDetail?.runMode, '') }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.licenseType')"
            >{{ getDictLabel('RULE_PLUGIN_INFO_LICENSE_TYPE', pluginInfoDetail?.licenseType, '') }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.licenseKey')"
          >
            <div>
              <Tooltip placement="top" :title="t('common.title.copy')">
                <span class="copy_btn" @click="handleCopyText"
                  ><SvgIcon name="copy" :size="12" /></span
              ></Tooltip>
              <p ref="textToCopy" style="height: 100px; overflow-y: auto">{{
                pluginInfoDetail.licenseKey
              }}</p>
            </div>
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.validUntil')"
          >
            {{ pluginInfoDetail?.validUntil }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.fileHash')"
          >
            {{ pluginInfoDetail?.fileHash }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.scanStatus')"
          >
            {{ pluginInfoDetail?.scanStatus }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.scanDate')"
          >
            {{ pluginInfoDetail?.scanDate }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.scanSummary')"
          >
            {{ pluginInfoDetail?.scanSummary }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.extendParams')"
          >
            {{ pluginInfoDetail?.extendParams }}
          </a-descriptions-item>
          <a-descriptions-item
            :labelStyle="labelStyle"
            :contentStyle="contentStyle"
            :label="t('iot.rule.plugin.pluginInfo.remark')"
          >
            {{ pluginInfoDetail?.remark }}
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </div>
    <div class="detail-info">
      <a-card :bordered="false">
        <a-tabs default-active-key="1" v-model:activeKey="currentKey" @change="handleChange">
          <a-tab-pane key="1" :tab="t('iot.rule.plugin.pluginInfo.quickInstallation')" />
          <a-tab-pane key="2" :tab="t('iot.rule.plugin.pluginInfo.pluginInstance')" />
        </a-tabs>
      </a-card>
      <a-row :gutter="[12, 0]" v-if="currentKey == '1'">
        <a-col :span="16">
          <a-card :bordered="false">
            <div class="plugin-left">
              <p class="label">{{ t('iot.rule.plugin.pluginInfo.instanceSpecification') }}</p>
              <div class="node-card-list">
                <div v-if="loading" class="loading">
                  <a-spin />
                </div>
                <div class="card-content">
                  <div
                    v-if="dataList.length > 0"
                    :gutter="[24, 12]"
                    style="max-height: 580px; overflow-y: auto; padding: 0 2px"
                  >
                    <a-row>
                      <a-col
                        v-for="record in dataList"
                        :key="record.id"
                        :lg="12"
                        :md="12"
                        :sm="24"
                        :xl="12"
                        :xs="12"
                        :xxl="12"
                      >
                        <div
                          class="card_wrap"
                          :class="record.id == selectNode.id ? 'active' : ''"
                          @click="handleSelectNode(record)"
                        >
                          <div class="header" :class="record.id == selectNode.id ? 'active' : ''">
                            <div class="name">{{ record.instanceName || '未命名' }}</div>
                            <div class="value ellipsis" :title="record.instanceIdentification">{{
                              record.instanceIdentification
                            }}</div>
                          </div>
                          <div class="conent">
                            <div class="item">
                              <span
                                >{{ t('iot.rule.plugin.pluginInstance.portRangeStart') }}：</span
                              >
                              <span>{{ record.portRangeStart }}</span>
                            </div>
                            <div class="item">
                              <span>{{ t('iot.rule.plugin.pluginInstance.portRangeEnd') }}：</span>
                              <span>{{ record.portRangeEnd }}</span>
                            </div>
                            <div class="item">
                              <span>{{ t('iot.rule.plugin.pluginInstance.machineIp') }}：</span>
                              <span>{{ record.machineIp }}</span>
                            </div>
                          </div>
                        </div>
                      </a-col>
                    </a-row>
                  </div>
                  <a-empty v-else />
                  <div class="card_pagination">
                    <a-pagination
                      v-model:current="current"
                      v-model:pageSize="pageSize"
                      :page-size-options="pageSizeOptions"
                      :show-total="(total) => t('component.table.total', { total })"
                      :total="total"
                      show-quick-jumper
                      show-size-changer
                      size="small"
                      @change="handleChangePagination"
                    />
                  </div>
                </div>
              </div>
            </div>
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card :bordered="false">
            <div class="plugin-right">
              <p class="label">{{ t('iot.rule.plugin.pluginInfo.configurationSummary') }}</p>
              <div class="item">
                <p>{{ t('iot.rule.plugin.pluginInstance.instanceName') }}</p>
                <p>{{ selectNode?.instanceName || '未命名' }}</p>
              </div>
              <div class="item">
                <p>{{ t('iot.rule.plugin.pluginInstance.instanceIdentification') }}</p>
                <p :title="selectNode.instanceIdentification">{{
                  selectNode.instanceIdentification || '--'
                }}</p>
              </div>
              <div class="item">
                <p>{{ t('iot.rule.plugin.pluginInstance.portRangeStart') }}</p>
                <p>{{ selectNode?.portRangeStart }}</p>
              </div>
              <div class="item">
                <p>{{ t('iot.rule.plugin.pluginInstance.portRangeEnd') }}</p>
                <p>{{ selectNode?.portRangeEnd }}</p>
              </div>
              <div class="item">
                <p>{{ t('iot.rule.plugin.pluginInstance.machineIp') }}</p>
                <p>{{ selectNode?.machineIp || '--' }}</p>
              </div>
              <a-button
                class="btn"
                type="primary"
                :disabled="!selectNode.id"
                danger
                @click="
                  handleInstall({
                    instanceId: selectNode.id,
                    status: 0,
                  })
                "
              >
                {{ t('iot.rule.plugin.pluginInfo.confirmInstallation') }}
              </a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <installPlugin
        :pluginInstanceDetailsList="pluginInfoDetail.pluginInstanceDetailsList"
        v-else-if="currentKey == '2'"
        @unInstall="handleInstall"
        @uninstallAll="handleInstallAll"
      />
    </div>
    <EditModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, reactive, computed, onMounted, nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import installPlugin from './installPlugin/index.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { detail, preload, install, installAll } from '/@/api/iot/rule/plugin/pluginInfo';
  import { page } from '/@/api/iot/rule/plugin/pluginInstance';
  import { PageWrapper } from '/@/components/Page';
  import { useRouter } from 'vue-router';
  import { DownOutlined, UpOutlined } from '@ant-design/icons-vue';
  import SvgIcon from '/@/components/Icon/src/SvgIcon.vue';
  import { Card, Row, Col, Descriptions, Tag, Tabs, Button, Tooltip } from 'ant-design-vue';
  import { useModal } from '/@/components/Modal';
  import { handleCopyTextV2 } from '/@/utils/thinglinks/common.tsx';
  import { columns } from './pluginInfo.data';
  import EditModal from './Edit.vue';
  import { useDict } from '/@/components/Dict';
  const { getDictLabel } = useDict();
  export default defineComponent({
    name: 'PluginInfoDetail',
    components: {
      ACard: Card,
      ARow: Row,
      ACol: Col,
      ATag: Tag,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
      [Tabs.name]: Tabs,
      [Tabs.TabPane.name]: Tabs.TabPane,
      PageWrapper,
      AButton: Button,
      EditModal,
      installPlugin,
      DownOutlined,
      UpOutlined,
      SvgIcon,
    },
    emits: ['success', 'register'],
    setup() {
      // 是否显示密码明文
      const { t } = useI18n();
      const textToCopy = ref(null);
      const getLoading = ref(false);
      const { createMessage, createConfirm } = useMessage();
      const { currentRoute } = useRouter();
      const [registerModal, { openModal }] = useModal();
      let pluginInfoDetail = reactive({});
      let id = ref('');
      let currentKey = ref('1');
      const descShow = ref(true);
      const current = ref<number>(1);
      const pageSize = ref<number>(20);
      const total = ref<number>(0);
      const dataList = ref<CardChannelInfoPageQuery[]>([]);
      const loading = ref<boolean>(false);
      const selectNode = ref({});
      const pageSizeOptions = ref(['10', '20', '30', '40', '50']);
      onMounted(() => {
        const { params } = currentRoute.value;
        id.value = params.id as string;
        load();
        getList();
      });
      const load = async () => {
        const res = await detail(id.value);
        pluginInfoDetail = Object.assign(pluginInfoDetail, res);
        await nextTick();
      };
      const handleChange = () => {
        load();
      };
      // 获取列表
      const getList = async (current: number, size: number, model = {}) => {
        loading.value = true;
        const res = await page({
          current,
          size,
          model,
          extra: {},
        });
        total.value = res.total;
        dataList.value = res.records;
      };
      const handleChangePagination = (page: number, size: number) => {
        current.value = page;
        pageSize.value = size;
      };
      const handleSelectNode = (record) => {
        console.log(record);
        selectNode.value = record;
      };
      // 预加载插件
      async function handlePreload() {
        // getLoading.value = true;
        createConfirm({
          iconType: 'warning',
          content: t('iot.rule.plugin.pluginInfo.tipsContentMsg'),
          okButtonProps: { type: 'default' },
          cancelButtonProps: { type: 'primary' },
          onOk: async () => {
            createMessage.warning(t('iot.rule.plugin.pluginInfo.tipsMsg'));
            try {
              await preload(id.value);
              getLoading.value = false;
              load();
              createMessage.success(t('iot.rule.plugin.pluginInfo.preloadingSuccessful'));
            } catch (error) {
              getLoading.value = false;
            }
          },
        });
      }

      const handleCopyText = async () => {
        const text = (textToCopy.value as any).innerText;
        handleCopyTextV2(text || '');
      };
      // 安装或卸载
      async function handleInstall(val) {
        // getLoading.value = true;
        createMessage.warning(t('iot.rule.plugin.pluginInfo.tipsMsg'));
        try {
          const { instanceId, status } = val;
          await install({
            pluginId: id.value,
            instanceId: instanceId,
            status: status,
          });
          getLoading.value = false;
          createMessage.success(
            status == 0
              ? t('iot.rule.plugin.pluginInfo.installationSuccessful')
              : t('iot.rule.plugin.pluginInfo.uninstallationSuccessful'),
          );
        } catch (error) {
          getLoading.value = false;
        }
      }

      // 卸载全部
      async function handleInstallAll() {
        createConfirm({
          iconType: 'warning',
          content: t('common.tips.confirmDelete'),
          onOk: async () => {
            getLoading.value = true;
            try {
              await installAll(id.value);
              getLoading.value = false;
              createMessage.success(t('iot.rule.plugin.pluginInfo.uninstallationSuccessful'));
            } catch (error) {
              getLoading.value = false;
            }
          },
        });
      }
      // 新增或编辑成功回调
      function handleSuccess() {
        load();
      }

      return {
        t,
        getLoading,
        pluginInfoDetail,
        currentKey,
        current,
        pageSize,
        total,
        dataList,
        pageSizeOptions,
        selectNode,
        getDictLabel,
        handlePreload,
        handleSuccess,
        registerModal,
        handleChangePagination,
        handleSelectNode,
        handleInstall,
        handleInstallAll,
        handleChange,
        textToCopy,
        handleCopyText,
        descShow,
        labelStyle: {
          'min-width': '140px',
          'font-weight': '600',
          'font-size': '14px',
        },
        contentStyle: {
          'max-width': '200px',
          'font-weight': '600',
          'font-size': '14px',
        },
      };
    },
  });
</script>
<style lang="less" scoped>
  .detail-info {
    padding: 16px 16px 0;

    .unfold {
      position: absolute;
      right: 20px;
      top: 20px;
    }

    &:nth-last-child(1) {
      padding-bottom: 16px;
    }

    .device_title {
      font-size: 16px;
      font-family: PingFang SC-Medium, PingFang SC;
      font-weight: 600;
      color: #2e3033;
      line-height: 19px;
      margin-bottom: 10px;
      display: flex;
      justify-content: space-between;
    }

    .base_data {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #a6a6a6;
      line-height: 17px;

      .item {
        padding-right: 12px;

        & + .item {
          padding-left: 12px;
          border-left: 1px solid #e0e0e0;
        }

        span {
          &.red {
            color: #fa3758;
          }

          &.green {
            color: #43cf7c;
          }

          &.orange {
            color: #cfa543;
          }
        }
      }
    }

    .copy_btn {
      text-align: right;
    }
  }

  .ts-img {
    width: 100%;
  }

  .label {
    font-weight: bold;
  }

  .plugin-left {
    display: flex;

    p {
      width: 100px;
    }

    .node-card-list {
      flex: 1;

      .card-content {
        display: flex;
        flex-direction: column;

        .card_wrap {
          display: flex;
          flex-direction: column;
          position: relative;
          background-image: url('/@/assets/images/iot/link/blue-bg.png');
          border: 1px solid #e8e8e8;
          border-radius: 5px;
          background-color: #fff;
          background-repeat: no-repeat;
          background-position: center center;
          background-size: 104% 104%;
          transition: all 0.5s;
          /* min-height: 228px; */
          margin-right: 10px;
          height: 100%;

          &.active {
            border: 1px @primary-color solid;
          }

          .header {
            padding: 8px 12px;
            border-bottom: 1px #e8e8e8 solid;

            &.active {
              border-bottom: 1px @primary-color solid;
              background-color: #f5f7fa;
            }
          }

          .conent {
            padding: 8px 12px;
          }

          &:hover {
            border-color: @primary-color;
            transform: scale(1.01);
            box-shadow: 0px 4px 12px rgba(0, 26, 51, 0.08);
          }

          .left_info {
            flex: 1;

            .item {
              margin-bottom: 4px;

              .label {
                font-size: 12px;
                color: #999;
              }

              .name {
                font-weight: 500;
              }

              .content {
                text-overflow: ellipsis;
                overflow: hidden;
                white-space: nowrap;
              }
            }
          }

          .type {
            position: absolute;
            top: 0;
            right: 0;
            border-radius: 0 8px 0 4px;
            padding: 2px 8px;

            &.error {
              background: #fad7d9;
              color: #d43030;
            }

            &.normal {
              background-color: #dce5f5;
              color: #1a66ff;
            }
          }

          .value {
            max-width: 190px;
          }
        }

        .card_pagination {
          align-self: flex-end;
        }
      }
    }
  }

  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }

  .plugin-right {
    position: relative;
    height: 600px;

    .item {
      display: flex;
      justify-content: space-between;

      p {
        max-width: 48%;
        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
      }
    }

    .btn {
      position: absolute;
      width: 100%;
      left: 0;
      bottom: 0;
    }
  }
</style>
