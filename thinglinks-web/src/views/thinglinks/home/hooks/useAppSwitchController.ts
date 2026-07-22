import { ref } from 'vue';

const visible = ref(false);

export function useAppSwitchController() {
  return {
    visible,
    open: () => {
      visible.value = true;
    },
    close: () => {
      visible.value = false;
    },
    toggle: () => {
      visible.value = !visible.value;
    },
  };
}
