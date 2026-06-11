<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="触发告警"
    :maskClosable="false"
    :destroyOnClose="true"
    :keyboard="true"
    :showOkBtn="false"
    :showCancelBtn="false"
    :height="600"
    width="1200px"
    @cancel="handleCancel"
  >
    <div class="container">
      <div class="header">
        <a-steps :current="current">
          <a-step v-for="item in steps" :key="item.key" :title="item.title" />
        </a-steps>
      </div>
      <div class="content">
        <div class="steps-content" v-show="current === 0">
          <AlarmListCardList
            type="select"
            @handle-select="handleSelect"
            :selectedValue="currentSelectedAlarmIdentification"
          />
        </div>
        <div class="steps-content" v-show="current === 1">
          <div class="form-item">
            <div class="label">{{ t('iot.link.engine.alarmRecord.alarmRecipient') }}:</div>
            <a-popover
              :title="t('iot.link.engine.linkage.input')"
              trigger="click"
              placement="topRight"
            >
              <template #content
                ><div class="add-phone">
                  <a-input v-model:value="inputPhone" /><a-button
                    type="primary"
                    @click="addInputPhone"
                    >{{ t('iot.link.engine.linkage.add') }}</a-button
                  >
                </div>
              </template>
              <div class="value">
                <div class="phone-item" v-for="item in resPhone" :key="item">
                  <span>{{ item }}</span>
                  <Icon
                    icon="ant-design:close-outlined"
                    :size="12"
                    class="close-icon"
                    @click="handleDeletePhone(item, $event)"
                  />
                </div>
              </div>
            </a-popover>
          </div>
          <div class="form-item">
            <div class="label"> {{ t('iot.link.engine.alarmRecord.alarmContent') }}: </div>
            <div class="editor">
              <TinymceCustom v-model="editorContent" :isOpen="ModalOpen" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <div>
        <a-button @click="handleCancel"> {{ t('common.cancelText') }} </a-button>
        <a-button v-show="current > 0" @click="prev">{{
          t('iot.link.engine.linkage.prev')
        }}</a-button>
        <a-button v-show="current < steps.length - 1" type="primary" @click="next">{{
          t('iot.link.engine.linkage.next')
        }}</a-button>
        <a-button v-show="current == steps.length - 1" type="primary" @click="handleSubmit">{{
          t('common.okText')
        }}</a-button>
      </div>
    </template>
  </BasicModal>
</template>
<script setup lang="ts">
  import { reactive, ref, watchEffect } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import AlarmListCardList from '../../../../../../../components/iot/rule/alarm/AlarmListCardList.vue';
  import { Icon } from '/@/components/Icon/index';
  import { TinymceCustom } from '/@/components/Tinymce/index';
  import { ActionEnum } from '/@/enums/commonEnum';

  const emit = defineEmits(['saveTriggerAlarm']);
  const currentType = ref<ActionEnum>(ActionEnum.ADD);
  const current = ref<number>(0);
  const currentSelectedAlarmIdentification = ref<string>('');
  const inputPhone = ref<string>('');
  const resPhone = ref<string[]>([]);
  const editorContent = ref<string>('');
  const actionIndex = ref<number>(0);
  const actionItem = reactive<any>({});
  const ModalOpen = ref<boolean[]>([false, false]);

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    actionIndex.value = data.actionIndex;
    Object.assign(actionItem, data.actionItem);
    currentType.value = data?.type;
    ModalOpen.value = [true, false];
  });

  watchEffect(() => {
    if (currentType.value === ActionEnum.EDIT) {
      currentSelectedAlarmIdentification.value = actionItem?.actionContent?.alarmIdentification;
      resPhone.value = actionItem?.actionContent?.atPhone?.split(',') || [];
      editorContent.value = actionItem?.actionContent?.contentData;
    }
  });

  const steps = [
    {
      key: '0',
      title: '选择告警规则',
    },
    {
      key: '1',
      title: '编辑告警数据',
    },
  ];
  const next = () => {
    if (currentSelectedAlarmIdentification.value == '') {
      createMessage.error('请选择告警规则');
      return;
    }
    current.value++;
    ModalOpen.value = [true, true];
  };
  const prev = () => {
    current.value--;
    ModalOpen.value = [true, false];
  };

  const resetHandle = () => {
    current.value = 0;
    currentType.value = ActionEnum.ADD;
    currentSelectedAlarmIdentification.value = '';
    inputPhone.value = '';
    resPhone.value = [];
    editorContent.value = '';
    actionIndex.value = 0;
    actionItem.value = {};
    ModalOpen.value = [false, false];
  };

  const handleSubmit = async () => {
    if (currentSelectedAlarmIdentification.value == '') {
      createMessage.error(t('common.chooseText') + t('iot.link.engine.alarmRecord.alarmRule'));
      return;
    }
    if (resPhone.value.length === 0) {
      createMessage.error(t('common.inputText') + t('iot.link.engine.alarmRecord.alarmRecipient'));
      return;
    }
    if (editorContent.value === '') {
      createMessage.error(t('common.inputText') + t('iot.link.engine.alarmRecord.alarmContent'));
      return;
    }
    emit('saveTriggerAlarm', {
      actionIndex: actionIndex.value,
      atPhone: resPhone.value?.join(','),
      alarmIdentification: currentSelectedAlarmIdentification.value,
      contentData: editorContent.value,
    });
    closeModal();
    resetHandle();
  };

  const handleCancel = () => {
    closeModal();
    resetHandle();
  };

  const handleSelect = (alarmIdentification: string) => {
    currentSelectedAlarmIdentification.value = alarmIdentification;
  };
  const addInputPhone = () => {
    // 定义正则表达式
    const phoneNumberPattern = /^1[3-9]\d{9}$/;
    if (!phoneNumberPattern.test(inputPhone.value)) {
      createMessage.error('请输入合法手机号码');
      return;
    }
    if (resPhone.value.includes(inputPhone.value)) {
      inputPhone.value = '';
      return;
    }
    resPhone.value = [...resPhone.value, inputPhone.value];
    inputPhone.value = '';
  };

  const handleDeletePhone = (phone: string, e: MouseEvent) => {
    e.stopPropagation();
    resPhone.value = resPhone.value.filter((item) => item !== phone);
  };
</script>
<style scoped lang="less">
  .container {
    height: 600px;
    display: flex;
    flex-direction: column;

    .header {
      height: 32px;
    }

    .content {
      padding: 20px 0;
      flex: 1;
      overflow: auto;
      margin-top: 10px;

      .steps-content {
        display: flex;
        flex-direction: column;
        gap: 20px;

        .form-item {
          display: flex;
          flex-direction: column;

          .label {
            width: 200px;
            margin-bottom: 8px;
          }

          .value {
            height: 32px;
            border: 1px solid #d9d9d9;
            display: flex;
            align-items: center;
            gap: 10px;
            cursor: pointer;
            padding: 0 8px;
            flex-wrap: nowrap;
            overflow: auto;
            -ms-overflow-style: none; /* IE 和 Edge */
            scrollbar-width: none; /* Firefox */
            &:hover {
              border-color: #1aa391;
            }

            &::-webkit-scrollbar {
              /* WebKit 浏览器 */
              display: none;
            }

            .phone-item {
              width: 116px;
              height: 22px;
              font-size: 12px;
              background-color: #f5f5f5;
              display: flex;
              align-items: center;
              padding: 0 4px;
              border-radius: 4px;

              .close-icon {
                cursor: pointer;
              }
            }
          }
        }
      }
    }
  }

  .add-phone {
    display: flex;
    gap: 8px;
  }
</style>
