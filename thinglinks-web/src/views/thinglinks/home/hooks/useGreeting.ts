/**
 * 按当前时间返回对应问候语的 i18n key 和日期信息。
 * 所有文本由组件层 t() 翻译；此 hook 不产生任何硬编码文案。
 *
 * 时间段划分：
 *   早晨 5-8 / 上午 8-11 / 中午 11-13 / 下午 13-18 / 晚上 18-23 / 夜里 23-5
 */
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { GREETINGS } from '../data';

const WEEK_I18N_KEYS = [
  'workbench.week.sun',
  'workbench.week.mon',
  'workbench.week.tue',
  'workbench.week.wed',
  'workbench.week.thu',
  'workbench.week.fri',
  'workbench.week.sat',
];

export function useGreeting(tickMs = 60_000) {
  const now = ref(Date.now());
  let timer: ReturnType<typeof setInterval> | null = null;

  onMounted(() => {
    timer = setInterval(() => (now.value = Date.now()), tickMs);
  });
  onBeforeUnmount(() => {
    if (timer) clearInterval(timer);
  });

  /** 问候语 i18n key */
  const greetingKey = computed(() => {
    const h = new Date(now.value).getHours();
    if (h >= 5 && h < 8) return GREETINGS.earlyMorning;
    if (h >= 8 && h < 11) return GREETINGS.morning;
    if (h >= 11 && h < 13) return GREETINGS.noon;
    if (h >= 13 && h < 18) return GREETINGS.afternoon;
    if (h >= 18 && h < 23) return GREETINGS.evening;
    return GREETINGS.night;
  });

  /** YYYY-MM-DD 纯数字日期 */
  const dateText = computed(() => {
    const d = new Date(now.value);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  });

  /** 星期几的 i18n key */
  const weekKey = computed(() => {
    const d = new Date(now.value);
    return WEEK_I18N_KEYS[d.getDay()];
  });

  return { greetingKey, dateText, weekKey, now };
}
