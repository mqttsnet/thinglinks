import type { Router } from 'vue-router';
import type { AppRouteModule } from '/@/router/types';

const TAG = '[路由诊断]';

function logWarn(...args: any[]): void {
  // eslint-disable-next-line no-console
  console.warn(TAG, ...args);
}

function logError(...args: any[]): void {
  // eslint-disable-next-line no-console
  console.error(TAG, ...args);
}

function logInfo(...args: any[]): void {
  if (process.env.NODE_ENV === 'production') {
    return;
  }
  // eslint-disable-next-line no-console
  console.info(TAG, ...args);
}

/**
 * 检查后端动态路由树的字段完整性与唯一性，发现问题打到控制台供管理员排查。
 *
 * 检查项：
 *  - {@code routerList} 是否为空
 *  - 每条路由是否缺少 {@code name} / {@code meta.title} / {@code code}
 *  - {@code name} 是否在树内重复
 *  - {@code path} 是否在树内重复
 *  - {@code code} 是否在树内重复
 */
export function diagnoseRouteList(routes: AppRouteModule[] | undefined): void {
  if (!routes || routes.length === 0) {
    logWarn(
      '后端 oauth/anyone/visible/resource 返回的 routerList 为空，所有动态菜单都不会注册。' +
        '请确认当前账号在该应用下有可见资源',
    );
    return;
  }

  const seenNames = new Map<string, AppRouteModule>();
  const seenPaths = new Map<string, AppRouteModule>();
  const seenCodes = new Map<string, AppRouteModule>();
  let totalCount = 0;
  let missingCodeCount = 0;

  function walk(list: AppRouteModule[]): void {
    for (const r of list) {
      totalCount += 1;
      const ctx = `path=${r.path}, component=${r.component}`;

      if (!r.name) {
        logWarn(`路由缺少 name 字段（${ctx}），任何 push({ name }) 都无法命中此条`);
      } else if (typeof r.name === 'string') {
        const dup = seenNames.get(r.name);
        if (dup) {
          logError(
            `路由 name 重复："${r.name}"\n` +
              `  第 1 处：path=${dup.path}, component=${dup.component}\n` +
              `  第 2 处：${ctx}\n` +
              `  → push({ name: "${r.name}" }) 会命中先注册的那条；请去【应用管理-资源管理】把菜单显示名改唯一`,
          );
        } else {
          seenNames.set(r.name, r);
        }
      }

      if (!r.meta?.title) {
        logWarn(`路由缺少 meta.title（${ctx}），菜单 / Tab 标题会显示空白`);
      }

      const code = ((r.meta as any)?.code as string | undefined) || ((r as any).code as string | undefined);
      if (!code && r.component !== 'LAYOUT') {
        missingCodeCount += 1;
        logWarn(
          `路由缺少 code 字段（${ctx}）。keep-alive cache key 会回退到 name，存在撞名风险；` +
            '若后端已部署 name+code 双字段版本仍出现此告警，请去【应用管理-资源管理】检查菜单资源编码是否为空',
        );
      } else if (code) {
        const dup = seenCodes.get(code);
        if (dup) {
          logError(
            `路由 code 重复："${code}"\n` +
              `  第 1 处：path=${dup.path}\n` +
              `  第 2 处：${ctx}\n` +
              `  keep-alive cache key 会冲突，多 tab 缓存可能互相覆盖；请去后台菜单数据排查菜单资源编码`,
          );
        } else {
          seenCodes.set(code, r);
        }
      }

      if (r.path && r.path !== '') {
        const dupPath = seenPaths.get(r.path);
        if (dupPath) {
          logError(
            `路由 path 重复："${r.path}"\n` +
              `  第 1 处：name=${String(dupPath.name)}\n` +
              `  第 2 处：name=${String(r.name)}`,
          );
        } else {
          seenPaths.set(r.path, r);
        }
      }

      if (r.children?.length) {
        walk(r.children);
      }
    }
  }

  walk(routes);

  logInfo(
    `动态路由健康检查完成：共 ${totalCount} 条；唯一 name=${seenNames.size}，唯一 path=${seenPaths.size}，唯一 code=${seenCodes.size}` +
      (missingCodeCount > 0 ? `；其中 ${missingCodeCount} 条缺少 code 字段` : ''),
  );
}

/**
 * 在 router 实例上拦截 push / replace，捕获导航失败时打印目标 + 模糊匹配候选。
 * 不吞错误，原始 reject 链路保留。
 */
export function installRouterDiagnostics(router: Router): void {
  const origPush = router.push.bind(router);
  const origReplace = router.replace.bind(router);

  function explain(method: 'push' | 'replace', target: any, err: any): void {
    const requested =
      typeof target === 'string'
        ? target
        : (target?.name ?? target?.path ?? '<unknown>');
    const allNames = router
      .getRoutes()
      .map((r) => r.name)
      .filter((n): n is string | symbol => Boolean(n))
      .map((n) => String(n));

    const reqStr = String(requested);
    const candidates = allNames
      .filter((n) => n !== reqStr && (n.includes(reqStr) || reqStr.includes(n)))
      .slice(0, 5);

    const lines = [
      `[路由导航失败] router.${method}(${JSON.stringify(target)}) → ${err?.message ?? err}`,
      candidates.length
        ? `  可能想跳转的是：${candidates.map((c) => `"${c}"`).join(', ')}`
        : '  当前路由表里找不到相似 name，可能是该资源未授权 / 后端菜单未配置 / 调用方 name 拼错',
      `  当前路由表共 ${allNames.length} 条，部分示例：${allNames.slice(0, 8).join(', ')}${
        allNames.length > 8 ? '...' : ''
      }`,
    ];
    // eslint-disable-next-line no-console
    console.error(lines.join('\n'));
  }

  router.push = ((to: any) => {
    const promise = origPush(to);
    if (promise?.catch) {
      promise.catch((err: any) => {
        explain('push', to, err);
        throw err;
      });
    }
    return promise;
  }) as Router['push'];

  router.replace = ((to: any) => {
    const promise = origReplace(to);
    if (promise?.catch) {
      promise.catch((err: any) => {
        explain('replace', to, err);
        throw err;
      });
    }
    return promise;
  }) as Router['replace'];
}
