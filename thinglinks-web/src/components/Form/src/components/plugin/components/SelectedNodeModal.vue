<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('iot.rule.plugin.pluginInfo.selectRunPlugin')"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    @ok="handleSubmit"
    @cancel="handleCancel"
    :width="1200"
  >
    <div class="node-card-list">
      <div v-if="loading" class="loading">
        <a-spin />
      </div>
      <div class="card-content">
        <a-row v-if="dataList.length > 0" :gutter="[24, 12]">
          <a-col
            v-for="record in dataList"
            :key="record.id"
            :lg="12"
            :md="12"
            :sm="24"
            :xl="8"
            :xs="24"
            :xxl="6"
          >
            <div
              class="card_wrap"
              :class="selectNode.instanceId === record.instanceId ? 'active' : ''"
              @click="handleSelectNode(record)"
            >
              <div class="left_info">
                <div class="item">
                  <div class="name ellipsis" :title="record.instanceId">{{
                    record.instanceId || '未命名'
                  }}</div>
                </div>
                <a-row :gutter="[4, 0]">
                  <a-col :span="8">
                    <div class="item">
                      <div class="label">{{
                        t('iot.rule.plugin.pluginInstance.applicationName')
                      }}</div>
                      <div class="content" :title="record.applicationName">{{
                        record.applicationName
                      }}</div>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="item">
                      <div class="label">{{ t('iot.rule.plugin.pluginInstance.weight') }}</div>
                      <div class="content" :title="record.weight">{{ record.weight }}</div>
                    </div>
                  </a-col>
                  <a-col :span="8">
                    <div class="item">
                      <div class="label">{{ t('iot.rule.plugin.pluginInstance.healthy') }}</div>
                      <div class="content" :title="record.healthy">{{ record.healthy }}</div>
                    </div>
                  </a-col>
                </a-row>
                <a-row :gutter="[4, 0]">
                  <a-col :span="12">
                    <div class="item">
                      <div class="label">ip</div>
                      <div class="content" :title="record.ip">{{ record.ip }}</div>
                    </div>
                  </a-col>
                  <a-col :span="12">
                    <div class="item">
                      <div class="label">{{ t('iot.rule.plugin.pluginInstance.port') }}</div>
                      <div class="content" :title="record.port">{{ record.port }}</div>
                    </div>
                  </a-col>
                </a-row>
              </div>
              <img
                class="right_img"
                src="../../../../../../assets/images/iot/rule/plugin/application.png"
                alt=""
              />
            </div>
          </a-col>
        </a-row>
        <a-empty v-else />
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, reactive, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { DictEnum } from '/@/enums/commonEnum';
  import { useDict } from '/@/components/Dict';
  import { available } from '/@/api/iot/rule/plugin/pluginInstance';
  import { useMessage } from '/@/hooks/web/useMessage';
  const props = defineProps({ value: String });
  const { t } = useI18n();
  const { getDictLabel } = useDict();
  const { createMessage } = useMessage();
  const [registerModal, { closeModal }] = useModalInner();
  const emits = defineEmits(['success', 'updateSelectNode']);
  const selectNode = reactive({});
  const dataList = ref([]);
  const loading = ref<boolean>(false);

  // 获取列表
  const getList = async () => {
    loading.value = true;
    const res = await available();
    dataList.value = res;
    loading.value = false;
  };

  const handleSelectNode = (record) => {
    Object.assign(selectNode, record);
  };

  const handleSubmit = () => {
    if (Object.keys(selectNode).length === 0) {
      createMessage.warning(t('video.media.proxy.pleaseSelectNode'));
      return;
    }
    emits('success', selectNode);
    closeModal();
  };
  const handleCancel = () => {
    emits('success', {});
    emits('updateSelectNode', { applicationName: '' });
    handleSelectNode({ instanceId: '' });
    closeModal();
  };

  // 监控 props.value 变化，设置 selectNode
  watch(
    () => props.value,
    async (newValue) => {
      // if (!newValue) {
      //   emits('updateSelectNode', { applicationName: '' });
      //   handleSelectNode({ applicationName: '' });
      //   return;
      // }
      if (dataList.value.length === 0) {
        await getList();
      }
      const selected = dataList.value.find((item) => item.instanceId === newValue);
      if (selected) {
        Object.assign(selectNode, selected);
        emits('updateSelectNode', selected);
      }
    },
    { immediate: true },
  );
</script>
<style lang="less" scoped>
  .node-card-list {
    background-color: #fff;
    padding: 22px;

    .loading {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }

    .card-content {
      display: flex;
      flex-direction: column;

      .card_wrap {
        display: flex;
        align-items: center;
        justify-content: space-between;
        position: relative;
        // background-image: url('/@/assets/images/iot/link/blue-bg.png');
        border: 1px solid #e8e8e8;
        padding: 8px 12px 8px;
        border-radius: 8px;
        background-color: #fff;
        background-repeat: no-repeat;
        background-position: center center;
        background-size: 104% 104%;
        transition: all 0.5s;
        // min-height: 228px;
        height: 100%;

        &:hover {
          border-color: #1a66ff;
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
              max-width: 180px;
            }

            .content {
              text-overflow: ellipsis;
              overflow: hidden;
              white-space: nowrap;
            }
          }
        }

        .right_img {
          width: 180px;
          height: 180px;
          margin-top: 24px;
          cursor: pointer;
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
      }

      .active {
        border: 1px solid #1a66ff;
      }

      .card_pagination {
        align-self: flex-end;
      }
    }

    .ellipsis {
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap;
    }
  }
</style>
