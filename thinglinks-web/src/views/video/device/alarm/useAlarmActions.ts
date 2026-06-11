import { h, ref } from 'vue';
import { Input, Modal } from 'ant-design-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import { confirmAlarm, ignoreAlarm, resolveAlarm } from '/@/api/video/device/alarm';

interface PromptOptions {
  title: string;
  placeholder: string;
}

/**
 * 告警「确认 / 解决 / 忽略」操作的共享逻辑。
 * 列表页（卡片/表格视图）与详情页复用，避免交互/文案漂移。
 */
export function useAlarmActions(onSuccess?: () => void | Promise<void>) {
  const { createMessage } = useMessage();
  const { t } = useI18n();

  /** 弹出带 TextArea 的输入确认框，返回用户输入（取消返回 null）。空值时提交按钮禁用。 */
  function openPromptModal(opts: PromptOptions): Promise<string | null> {
    return new Promise((resolve) => {
      const inputValue = ref('');
      let modal: { destroy: () => void; update: (cfg: any) => void } | undefined;
      modal = Modal.confirm({
        title: opts.title,
        icon: null,
        centered: true,
        okText: t('video.device.alarm.submit'),
        cancelText: t('video.device.alarm.cancel'),
        okButtonProps: { disabled: true },
        content: h(Input.TextArea, {
          rows: 4,
          maxlength: 200,
          showCount: true,
          placeholder: opts.placeholder,
          'onUpdate:value': (v: string) => {
            inputValue.value = v ?? '';
            modal?.update({ okButtonProps: { disabled: !inputValue.value.trim() } });
          },
        }),
        onOk: () => {
          const trimmed = inputValue.value.trim();
          if (!trimmed) return;
          resolve(trimmed);
        },
        onCancel: () => resolve(null),
      }) as any;
    });
  }

  async function handleConfirm(record: Record<string, any>) {
    if (Number(record.handleStatus) !== 0) return;
    const result = await openPromptModal({
      title: t('video.device.alarm.confirmTitle'),
      placeholder: t('video.device.alarm.inputHandleDesc'),
    });
    if (!result) return;
    await confirmAlarm(record.id, result);
    createMessage.success(t('video.device.alarm.confirmed'));
    await onSuccess?.();
  }

  async function handleResolve(record: Record<string, any>) {
    if (Number(record.handleStatus) !== 1) return;
    const result = await openPromptModal({
      title: t('video.device.alarm.resolveTitle'),
      placeholder: t('video.device.alarm.inputResolveDesc'),
    });
    if (!result) return;
    await resolveAlarm(record.id, result);
    createMessage.success(t('video.device.alarm.resolved'));
    await onSuccess?.();
  }

  async function handleIgnore(record: Record<string, any>) {
    const status = Number(record.handleStatus);
    if (status !== 0 && status !== 1) return;
    const result = await openPromptModal({
      title: t('video.device.alarm.ignoreTitle'),
      placeholder: t('video.device.alarm.inputIgnoreReason'),
    });
    if (!result) return;
    await ignoreAlarm(record.id, result);
    createMessage.success(t('video.device.alarm.ignoredSuccess'));
    await onSuccess?.();
  }

  return {
    openPromptModal,
    handleConfirm,
    handleResolve,
    handleIgnore,
  };
}
