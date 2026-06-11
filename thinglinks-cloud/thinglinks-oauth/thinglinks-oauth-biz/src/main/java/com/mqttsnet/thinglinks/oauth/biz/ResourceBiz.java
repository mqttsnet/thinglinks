package com.mqttsnet.thinglinks.oauth.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.CollHelper;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.basic.utils.TreeUtil;
import com.mqttsnet.thinglinks.base.service.system.BaseRoleService;
import com.mqttsnet.thinglinks.base.vo.result.user.RouterMeta;
import com.mqttsnet.thinglinks.base.vo.result.user.VueRouter;
import com.mqttsnet.thinglinks.common.constant.BizConstant;
import com.mqttsnet.thinglinks.common.constant.RoleConstant;
import com.mqttsnet.thinglinks.model.enumeration.system.ResourceTypeEnum;
import com.mqttsnet.thinglinks.system.entity.application.DefApplication;
import com.mqttsnet.thinglinks.system.entity.application.DefResource;
import com.mqttsnet.thinglinks.system.enumeration.system.ClientTypeEnum;
import com.mqttsnet.thinglinks.system.enumeration.tenant.ResourceOpenWithEnum;
import com.mqttsnet.thinglinks.system.service.application.DefApplicationService;
import com.mqttsnet.thinglinks.system.service.application.DefResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资源大业务
 *
 * @author mqttsnet
 * @date 2021/10/29 19:42
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceBiz {
    private final DefResourceService defResourceService;
    private final DefApplicationService defApplicationService;
    private final BaseRoleService baseRoleService;

    /**
     * 是否所有的子都是视图
     *
     * 若某个菜单的子集，全部都是视图，这需要标记为隐藏子集
     *
     */
    private static boolean hideChildrenInMenu(List<VueRouter> children) {
        if (CollUtil.isEmpty(children)) {
            return false;
        }

        // hidden： true - 视图   false - 菜单  null - 菜单

        return children.stream().allMatch(item -> item.getIsHidden() != null && item.getIsHidden());
    }

    /**
     * 查询当前用户可用的资源
     *
     * @param applicationId 应用id
     * @return 资源树
     */
    public List<String> findVisibleResource(Long employeeId, Long applicationId) {
        // 用户还没成为员工时登录，租户id为0
        boolean emptyTenant = ContextUtil.isEmptyTenantId();
        if (emptyTenant) {
            return Collections.emptyList();
        }
        List<DefResource> list;
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        List<String> resourceCodes = Collections.emptyList();
        if (isAdmin) {
            // 管理员 拥有所有权限，查询指定应用，指定类型的 所有资源
            list = defResourceService.findResourceListByTenantIdAndApplicationIds(ContextUtil.getTenantId(),
                    applicationId != null ? Collections.singletonList(applicationId) : Collections.emptyList(), resourceCodes);
        } else {
            List<Long> resourceIdList = baseRoleService.findResourceIdByEmployeeId(applicationId, employeeId);
            if (resourceIdList.isEmpty()) {
                return Collections.emptyList();
            }

            list = defResourceService.findByIdsAndType(ContextUtil.getTenantId(), applicationId, resourceIdList, resourceCodes);
        }
        return CollHelper.split(list, DefResource::getCode, StrPool.SEMICOLON);
    }

    public List<VueRouter> findAllVisibleRouter(Long employeeId, String subGroup, ClientTypeEnum type) {
        boolean emptyTenant = ContextUtil.isEmptyTenantId();
        if (emptyTenant) {
            return Collections.emptyList();
        }

        List<DefResource> list;
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        List<String> menuCodes = Collections.singletonList(ResourceTypeEnum.MENU.getCode());

        List<DefApplication> applicationList = defApplicationService.list(Wraps.<DefApplication>lbQ().orderByAsc(DefApplication::getSortValue));
        List<VueRouter> treeList = new ArrayList<>();
        for (DefApplication defApplication : applicationList) {
            if (isAdmin) {
                // 管理员 拥有所有权限，查询指定应用，指定类型的 所有路由
                list = defResourceService.
                        findResourceListByTenantIdAndApplicationIds(ContextUtil.getTenantId(),
                                Collections.singletonList(defApplication.getId()), menuCodes);
            } else {
                List<Long> resourceIdList = baseRoleService.findResourceIdByEmployeeId(defApplication.getId(), employeeId);
                if (resourceIdList.isEmpty()) {
                    continue;
                }

                list = defResourceService.findByIdsAndType(resourceIdList, menuCodes);
            }

            if (StrUtil.isNotEmpty(subGroup)) {
                list = list.stream().filter(item -> subGroup.equals(item.getSubGroup())).toList();
            }

            if (list.isEmpty()) {
                continue;
            }

            List<VueRouter> routers = BeanPlusUtil.copyToList(list, VueRouter.class);
            List<VueRouter> tree = TreeUtil.buildTree(routers);
            if (ClientTypeEnum.THINGLINKS_WEB_SOYBEAN.eq(type)) {
                forEachTreeBySoybean(tree, 1, null);
            } else if (ClientTypeEnum.THINGLINKS_WEB_VBEN5.eq(type)) {
                forEachTreeByVben5(tree, 1, null);
            } else {
                forEachTree(tree, 1);
            }
            populateRouteCode(tree, list);

            VueRouter applicationRouter = new VueRouter();
            applicationRouter.setName(defApplication.getName());
            applicationRouter.setPath("/" + defApplication.getId());
            applicationRouter.setComponent(BizConstant.LAYOUT);
            VueRouter childrenFirst = getChildrenFirst(tree);
            applicationRouter.setRedirect(StrUtil.isNotEmpty(defApplication.getRedirect()) ? defApplication.getRedirect() : (childrenFirst != null ? childrenFirst.getPath() : null));

            RouterMeta meta = new RouterMeta();
            meta.setTitle(defApplication.getName());
            meta.setHideMenu(false);
            meta.setHideInMenu(false);
            // 是否所有的子都是视图
            meta.setHideChildrenInMenu(false);

            applicationRouter.setMeta(meta);
            applicationRouter.setResourceType(ResourceTypeEnum.MENU.getCode());
            applicationRouter.setOpenWith(ResourceOpenWithEnum.INNER_COMPONENT.getCode());

            applicationRouter.setChildren(tree);
            treeList.add(applicationRouter);
        }

        return treeList;
    }

    private VueRouter getChildrenFirst(List<VueRouter> list) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        VueRouter vueRouter = list.get(0);
        if (CollUtil.isEmpty(vueRouter.getChildren())) {
            return vueRouter;
        }
        return getChildrenFirst(vueRouter.getChildren());
    }

    /**
     * 查询当前用户可用的 菜单 + 视图
     * <p>
     * 1. 租户管理员拥有指定应用的所有 资源
     * 2. 还没成为员工的用户，没有任何 资源
     *
     * @param applicationId 应用id
     * @return 资源树
     */
    public List<VueRouter> findVisibleRouter(Long applicationId, Long employeeId, String subGroup, ClientTypeEnum type) {
        // 用户还没成为员工时登录，租户id为0
        boolean emptyTenant = ContextUtil.isEmptyTenantId();
        if (emptyTenant) {
            return Collections.emptyList();
        }
        List<DefResource> list;
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        List<String> menuCodes = Collections.singletonList(ResourceTypeEnum.MENU.getCode());
        if (isAdmin) {
            // 管理员 拥有所有权限，查询指定应用，指定类型的 所有路由
            list = defResourceService.findResourceListByTenantIdAndApplicationIds(ContextUtil.getTenantId(),
                    applicationId != null ? Collections.singletonList(applicationId) : Collections.emptyList(), menuCodes);
        } else {
            List<Long> resourceIdList = baseRoleService.findResourceIdByEmployeeId(applicationId, employeeId);
            if (resourceIdList.isEmpty()) {
                return Collections.emptyList();
            }

            list = defResourceService.findByIdsAndType(ContextUtil.getTenantId(), applicationId, resourceIdList, menuCodes);
        }

        if (StrUtil.isNotEmpty(subGroup)) {
            list = list.stream().filter(item -> subGroup.equals(item.getSubGroup())).toList();
        }

        List<VueRouter> routers = BeanPlusUtil.copyToList(list, VueRouter.class);
        List<VueRouter> tree = TreeUtil.buildTree(routers);
        if (ClientTypeEnum.THINGLINKS_WEB_SOYBEAN.eq(type)) {
            forEachTreeBySoybean(tree, 1, null);
        } else if (ClientTypeEnum.THINGLINKS_WEB_VBEN5.eq(type)) {
            forEachTreeByVben5(tree, 1, null);
        } else {
            forEachTree(tree, 1);
        }
        populateRouteCode(tree, list);
        return tree;
    }

    /**
     * 把菜单资源编码写入 {@link VueRouter#getCode()} 顶层字段及 {@code meta.code}，
     * {@link VueRouter#getName()} 保留菜单显示标题。
     *
     * @param tree    已构建并处理好 meta 的路由树
     * @param sources 原始 DefResource 列表，提供 id → code 映射
     */
    private void populateRouteCode(List<VueRouter> tree, List<DefResource> sources) {
        if (CollUtil.isEmpty(tree) || CollUtil.isEmpty(sources)) {
            return;
        }
        Map<Long, String> idToCode = sources.stream()
                .filter(s -> StrUtil.isNotBlank(s.getCode()))
                .collect(Collectors.toMap(DefResource::getId, DefResource::getCode, (a, b) -> a));
        walkAndFillCode(tree, idToCode);
    }

    private void walkAndFillCode(List<VueRouter> tree, Map<Long, String> idToCode) {
        if (CollUtil.isEmpty(tree)) {
            return;
        }
        for (VueRouter item : tree) {
            String code = idToCode.get(item.getId());
            if (StrUtil.isNotBlank(code)) {
                item.setCode(code);
                if (item.getMeta() != null) {
                    item.getMeta().setCode(code);
                }
            }
            walkAndFillCode(item.getChildren(), idToCode);
        }
    }


    /**
     * 查询员工是否拥有指定应用
     *
     * COLUMN 模式和DATASOURCE模式 此方法有所不同
     *
     * @param employeeId    员工id
     * @param applicationId 应用ID
     * @return 是否拥有指定应用
     */
    public Boolean checkApplication(Long applicationId, Long employeeId) {
        List<Long> applicationList = defApplicationService.findApplicationByEmployeeId(employeeId);
        return applicationList.contains(applicationId);
    }

    private void forEachTree(List<VueRouter> tree, int level) {
        if (CollUtil.isEmpty(tree)) {
            return;
        }
        for (VueRouter item : tree) {
            log.debug("level={}, label={}", level, item.getName());
            RouterMeta meta = null;
            if (StrUtil.isNotEmpty(item.getMetaJson()) && !StrPool.BRACE.equals(item.getMetaJson())) {
                meta = JsonUtil.parse(item.getMetaJson(), RouterMeta.class);
            }
            if (meta == null && item.getMeta() != null) {
                meta = item.getMeta();
            }
            if (meta == null) {
                meta = new RouterMeta();
            }
            meta.setComponent(item.getComponent());
            if (StrUtil.isEmpty(meta.getTitle())) {
                meta.setTitle(item.getName());
            }
            meta.setIcon(item.getIcon());
            if (ResourceOpenWithEnum.INNER_CHAIN.eq(item.getOpenWith())) {
                //  是否内嵌页面
                meta.setFrameSrc(item.getLink());
                item.setComponent(BizConstant.IFRAME);
                meta.setComponent("sys/iframe/index");
            } else if (ResourceOpenWithEnum.OUTER_CHAIN.eq(item.getOpenWith())) {
                // 是否外链
                item.setComponent(BizConstant.IFRAME);
                item.setPath(item.getLink());
            }

            // 视图需要隐藏
            meta.setHideMenu(item.getIsHidden() != null ? item.getIsHidden() : false);

            // 是否所有的子都是视图
            meta.setHideChildrenInMenu(hideChildrenInMenu(item.getChildren()));
            item.setMeta(meta);

            // 若当前菜单的 子菜单至少有一个菜单，将它设置为 LAYOUT
            if (CollUtil.isNotEmpty(item.getChildren()) && !hideChildrenInMenu(item.getChildren())) {
                item.setComponent(BizConstant.LAYOUT);
            }

            if (CollUtil.isNotEmpty(item.getChildren())) {
                forEachTree(item.getChildren(), level + 1);
            }
        }
    }

    private void forEachTreeByVben5(List<VueRouter> tree, int level, VueRouter parent) {
        if (CollUtil.isEmpty(tree)) {
            return;
        }
        for (VueRouter item : tree) {
            log.debug("level={}, label={}", level, item.getName());
            RouterMeta meta = null;
            if (StrUtil.isNotEmpty(item.getMetaJson()) && !StrPool.BRACE.equals(item.getMetaJson())) {
                meta = JsonUtil.parse(item.getMetaJson(), RouterMeta.class);
            }
            if (meta == null && item.getMeta() != null) {
                meta = item.getMeta();
            }
            if (meta == null) {
                meta = new RouterMeta();
            }
            meta.setComponent(item.getComponent());
            if (StrUtil.isEmpty(meta.getTitle())) {
                meta.setTitle(item.getName());
            }
            meta.setIcon(item.getIcon());
            if (ResourceOpenWithEnum.INNER_CHAIN.eq(item.getOpenWith())) {
                //  是否内嵌页面
                meta.setIframeSrc(item.getLink());
                item.setComponent(BizConstant.IFRAME);
            } else if (ResourceOpenWithEnum.OUTER_CHAIN.eq(item.getOpenWith())) {
                // 是否外链
                meta.setLink(item.getLink());
                item.setComponent(BizConstant.IFRAME);
            }

            // 视图需要隐藏
            meta.setHideInMenu(item.getIsHidden() != null ? item.getIsHidden() : false);

            if (StrUtil.isNotEmpty(meta.getCurrentActiveMenu())) {
                meta.setActivePath(meta.getCurrentActiveMenu());
            } else {
                if (meta.getHideInMenu() && StrUtil.isEmpty(meta.getActivePath()) && parent != null) {
                    meta.setActivePath(parent.getPath());
                }
            }

            // 是否所有的子都是视图
            meta.setHideChildrenInMenu(hideChildrenInMenu(item.getChildren()));
            item.setMeta(meta);

            if (CollUtil.isNotEmpty(item.getChildren())) {
                String component = item.getComponent();
                List<VueRouter> childrenList = item.getChildren();
                // 是否所有的子都是视图
                boolean allView = hideChildrenInMenu(item.getChildren());
                item.setComponent(BizConstant.LAYOUT);
                if (allView) {
                    VueRouter first = new VueRouter();
                    first.setName(meta.getTitle() + "Child");
                    first.setPath("");
                    first.setComponent(component);
                    RouterMeta firstMeta = BeanPlusUtil.toBean(item.getMeta(), RouterMeta.class);
                    firstMeta.setActivePath(item.getPath()).setHideInMenu(true).setTitle(meta.getTitle());
                    first.setMeta(firstMeta);
                    first.setIsHidden(true);
                    first.setIcon(firstMeta.getIcon());

                    childrenList.add(0, first);
                    item.setChildren(childrenList);
                }
            }

            // 若当前菜单的 子菜单至少有一个菜单，将它设置为 null
            if (CollUtil.isNotEmpty(item.getChildren()) && !hideChildrenInMenu(item.getChildren())) {
                item.setComponent(BizConstant.LAYOUT);
            }

            if (CollUtil.isNotEmpty(item.getChildren())) {
                forEachTreeByVben5(item.getChildren(), level + 1, item);
            }
        }
    }


    private void forEachTreeBySoybean(List<VueRouter> tree, int level, VueRouter parent) {
        if (CollUtil.isEmpty(tree)) {
            return;
        }
        for (VueRouter item : tree) {
            log.debug("level={}, label={}", level, item.getName());
            RouterMeta meta = null;
            if (item.getMeta() == null) {
                if (StrUtil.isNotEmpty(item.getMetaJson()) && !StrPool.BRACE.equals(item.getMetaJson())) {
                    meta = JsonUtil.parse(item.getMetaJson(), RouterMeta.class);
                }
                if (meta == null && item.getMeta() != null) {
                    meta = item.getMeta();
                }
                if (meta == null) {
                    meta = new RouterMeta();
                }
                meta.setComponent(item.getComponent());
                if (StrUtil.isEmpty(meta.getTitle())) {
                    meta.setTitle(item.getName());
                }
                meta.setIcon(item.getIcon());

                // 视图需要隐藏
                meta.setHideInMenu(item.getIsHidden() != null ? item.getIsHidden() : false);

                if (meta.getHideInMenu() && StrUtil.isEmpty(meta.getActiveMenu()) && parent != null) {
                    meta.setActiveMenu(parent.getName());
                }

                if (ResourceOpenWithEnum.INNER_CHAIN.eq(item.getOpenWith())) {
                    //  是否内嵌页面
                    meta.setFrameSrc(item.getLink());
                    item.setComponent(BizConstant.IFRAME.toLowerCase());
                    meta.setComponent("_builtin/iframe/index");
                } else if (ResourceOpenWithEnum.OUTER_CHAIN.eq(item.getOpenWith())) {
                    // 是否外链
                    item.setComponent(BizConstant.IFRAME.toLowerCase());
                    meta.setHref(item.getLink());
                }

            } else {
                meta = item.getMeta();
            }

            meta.setI18nKey(item.getName());


            if (parent != null) {
                item.setName(parent.getName() + "_" + item.getName());
            }

            item.setMeta(meta);

            if (CollUtil.isNotEmpty(item.getChildren())) {
                String component = item.getComponent();
                List<VueRouter> childrenList = item.getChildren();
                // 是否所有的子都是视图
                boolean allView = hideChildrenInMenu(item.getChildren());
                item.setComponent("layout.base");
                if (allView) {
                    VueRouter first = new VueRouter();
                    first.setName(meta.getTitle());
                    first.setPath(item.getPath() + "/children");
                    first.setComponent(component);
                    RouterMeta firstMeta = BeanPlusUtil.toBean(item.getMeta(), RouterMeta.class);
                    firstMeta.setActiveMenu(item.getName())
                            .setHideInMenu(true);
                    first.setMeta(firstMeta);

                    first.setIsHidden(true);
                    first.setIcon(firstMeta.getIcon());

                    childrenList.add(0, first);
                    item.setRedirect(first.getPath());
                    item.setChildren(childrenList);
                }
            } else {
                if (level == 1) {
                    item.setComponent("layout.base$" + item.getComponent());
                }
            }

            if (CollUtil.isNotEmpty(item.getChildren())) {
                forEachTreeBySoybean(item.getChildren(), level + 1, item);
            }
        }
    }


    /**
     * 检测员工是否拥有指定应用的权限
     *
     * @param applicationId 应用id
     * @param employeeId    员工id
     * @return 是否拥有指定应用的权限
     */
    public Boolean checkEmployeeHaveApplication(Long employeeId, Long applicationId) {
        // 用户还没成为员工时登录，租户id为0
        boolean emptyTenant = ContextUtil.isEmptyTenantId();
        if (emptyTenant) {
            return false;
        }
        boolean isAdmin = baseRoleService.checkRole(employeeId, RoleConstant.TENANT_ADMIN);
        if (isAdmin) {
            return true;
        }
        return CollUtil.isNotEmpty(baseRoleService.findResourceIdByEmployeeId(applicationId, employeeId));
    }


}
