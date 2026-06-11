import type { ModalFunc, ModalFuncProps } from 'ant-design-vue/lib/modal/Modal';

import { Modal, message as Message, notification } from 'ant-design-vue';
import {
  InfoCircleFilled,
  CheckCircleFilled,
  CloseCircleFilled,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons-vue';

import { NotificationArgsProps, ConfigProps } from 'ant-design-vue/lib/notification';
import { useI18n } from './useI18n';
import { isString } from '/@/utils/is';
import { h } from 'vue';

export interface NotifyApi {
  info(config: NotificationArgsProps): void;
  success(config: NotificationArgsProps): void;
  error(config: NotificationArgsProps): void;
  warn(config: NotificationArgsProps): void;
  warning(config: NotificationArgsProps): void;
  open(args: NotificationArgsProps): void;
  close(key: String): void;
  config(options: ConfigProps): void;
  destroy(): void;
}

export declare type NotificationPlacement = 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight';
export declare type IconType = 'success' | 'info' | 'error' | 'warning';
export interface ModalOptionsEx extends Omit<ModalFuncProps, 'iconType'> {
  iconType: 'warning' | 'success' | 'error' | 'info';
}
export type ModalOptionsPartial = Partial<ModalOptionsEx> & Pick<ModalOptionsEx, 'content'>;

interface ConfirmOptions {
  info: ModalFunc;
  success: ModalFunc;
  error: ModalFunc;
  warn: ModalFunc;
  warning: ModalFunc;
}

function getIcon(iconType: string) {
  if (iconType === 'warning') {
    return <InfoCircleFilled class="modal-icon-warning" />;
  } else if (iconType === 'success') {
    return <CheckCircleFilled class="modal-icon-success" />;
  } else if (iconType === 'info') {
    return <InfoCircleFilled class="modal-icon-info" />;
  } else {
    return <CloseCircleFilled class="modal-icon-error" />;
  }
}

function renderContent({ content }: Pick<ModalOptionsEx, 'content'>) {
  if (isString(content)) {
    return <div innerHTML={`<div>${content as string}</div>`}></div>;
  } else {
    return content;
  }
}

/**
 * @description: Create confirmation box
 */
function createConfirm(options: ModalOptionsEx): ConfirmOptions {
  const iconType = options.iconType || 'warning';
  Reflect.deleteProperty(options, 'iconType');
  const opt: ModalFuncProps = {
    centered: true,
    icon: getIcon(iconType),
    ...options,
    content: renderContent(options),
  };
  return Modal.confirm(opt) as unknown as ConfirmOptions;
}

const getBaseOptions = () => {
  const { t } = useI18n();
  return {
    okText: t('common.okText'),
    centered: true,
  };
};

function createModalOptions(options: ModalOptionsPartial, icon: string): ModalOptionsPartial {
  return {
    ...getBaseOptions(),
    ...options,
    content: renderContent(options),
    icon: getIcon(icon),
  };
}

function createSuccessModal(options: ModalOptionsPartial) {
  return Modal.success(createModalOptions(options, 'success'));
}

function createErrorModal(options: ModalOptionsPartial) {
  return Modal.error(createModalOptions(options, 'error'));
}

function createInfoModal(options: ModalOptionsPartial) {
  return Modal.info(createModalOptions(options, 'info'));
}

function createWarningModal(options: ModalOptionsPartial) {
  return Modal.warning(createModalOptions(options, 'warning'));
}

notification.config({
  placement: 'topRight',
  duration: 3,
  top: '90px', // 避开固定 Header(48px) + Tab栏，防止被布局层遮挡
});

// ─── 全局消息通知（Flexy Dashboard 风格）──────────────────────────
// 白底 + 彩色图标点缀，极简克制，对齐 Flexy 设计语言。

const MSG_ICON_COLORS = {
  success: '#13deb9',
  error: '#fa896b',
  warning: '#ffae1f',
  info: '#5d87ff',
};

const MSG_ICONS = {
  success: CheckCircleOutlined,
  error: CloseCircleOutlined,
  warning: ExclamationCircleOutlined,
  info: InfoCircleOutlined,
};

/**
 * 统一通知入口：右上角 Notification，纯字符串调用。
 *
 * @param type     消息类型
 * @param content  消息文本
 * @param duration 自动关闭延迟(秒)，默认 3
 */
function showStyledNotification(
  type: 'success' | 'error' | 'warning' | 'info',
  content: string,
  duration?: number,
) {
  const iconColor = MSG_ICON_COLORS[type];
  const IconComp = MSG_ICONS[type];

  notification[type]({
    message: content || '',
    description: undefined,
    icon: h(IconComp, { style: { color: iconColor, fontSize: '18px' } }),
    duration: duration ?? 3,
    placement: 'topRight',
    class: `styled-notify styled-notify-${type}`,
  });
}

/**
 * createMessage —— 全局消息提示
 *
 * 统一调用方式：createMessage.success('操作成功')
 * success / error / warning / info → 右上角 Notification（Flexy 风格）
 * loading → 顶部居中 Message（原生 ant-design-vue）
 */
const createMessage = {
  success: (content: string, duration?: number) => showStyledNotification('success', content, duration),
  error: (content: string, duration?: number) => showStyledNotification('error', content, duration),
  warning: (content: string, duration?: number) => showStyledNotification('warning', content, duration),
  warn: (content: string, duration?: number) => showStyledNotification('warning', content, duration),
  info: (content: string, duration?: number) => showStyledNotification('info', content, duration),
  loading: (...args: any[]) => (Message.loading as any)(...args),
};

/**
 * @description: message
 */
export function useMessage() {
  return {
    createMessage,
    notification: notification as NotifyApi,
    createConfirm: createConfirm,
    createSuccessModal,
    createErrorModal,
    createInfoModal,
    createWarningModal,
  };
}
