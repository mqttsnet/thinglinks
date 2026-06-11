import type { FormSchema } from '/@/components/Form';
import type { MqttActionConfigDto } from '../dto';
import type { BridgeOutboundActionModule } from './types';
import { tProto, tWithOverride, dsDefaultPlaceholder } from './i18n';

const tk = tProto('mqtt');

export const mqttBridgeAction: BridgeOutboundActionModule<MqttActionConfigDto> = {
  type: 'MQTT',

  actionFields(): FormSchema[] {
    return [
      {
        label: tWithOverride(tk('topicTemplate.label')),
        field: 'ac_mqtt_topicTemplate',
        component: 'Input',
        componentProps: { placeholder: dsDefaultPlaceholder() },
      },
      {
        label: tWithOverride(tk('qos.label')),
        field: 'ac_mqtt_qos',
        component: 'Select',
        componentProps: {
          placeholder: dsDefaultPlaceholder(),
          allowClear: true,
          options: [
            { label: tk('qos.opt_0'), value: 0 },
            { label: tk('qos.opt_1'), value: 1 },
            { label: tk('qos.opt_2'), value: 2 },
          ],
        },
      },
      {
        label: tWithOverride(tk('retained.label')),
        field: 'ac_mqtt_retained',
        component: 'Switch',
      },
    ];
  },

  assembleAction(values) {
    return {
      topicTemplate: values.ac_mqtt_topicTemplate,
      qos: values.ac_mqtt_qos,
      retained: values.ac_mqtt_retained,
    };
  },

  flattenAction(dto) {
    return {
      ac_mqtt_topicTemplate: dto?.topicTemplate,
      ac_mqtt_qos: dto?.qos,
      ac_mqtt_retained: dto?.retained ?? false,
    };
  },

  presetToFlat(preset) {
    return preset
      ? {
          ac_mqtt_topicTemplate: preset.topicTemplate,
          ac_mqtt_qos: preset.qos,
          ac_mqtt_retained: preset.retained ?? false,
        }
      : {};
  },
};
