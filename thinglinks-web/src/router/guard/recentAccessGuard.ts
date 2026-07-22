import type { Router } from 'vue-router';
import { useRecentAccessStore } from '/@/views/thinglinks/home/hooks/useRecentAccessStore';

export function createRecentAccessGuard(router: Router) {
  router.afterEach((to) => {
    try {
      const { record } = useRecentAccessStore();
      record(to);
    } catch {
      /* ignore */
    }
    return true;
  });
}
