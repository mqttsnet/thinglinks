package com.mqttsnet.thinglinks.video.service.group.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroup;
import com.mqttsnet.thinglinks.video.manager.group.VideoDeviceGroupManager;
import com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupService;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 设备分组业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoDeviceGroupServiceImpl extends SuperServiceImpl<VideoDeviceGroupManager, Long, VideoDeviceGroup> implements VideoDeviceGroupService {

    @Override
    public List<VideoDeviceGroup> listByParentId(Long parentId) {
        return superManager.listByParentId(parentId);
    }

    @Override
    public List<VideoDeviceGroup> listAllEnabled() {
        return superManager.listAllEnabled();
    }

    @Override
    public List<VideoDeviceGroupResultVO> buildTree() {
        List<VideoDeviceGroup> allGroups = listAllEnabled();
        List<VideoDeviceGroupResultVO> voList = BeanUtil.copyToList(allGroups, VideoDeviceGroupResultVO.class);

        // 使用 LinkedHashMap 保持排序
        Map<Long, VideoDeviceGroupResultVO> idMap = new LinkedHashMap<>();
        for (VideoDeviceGroupResultVO vo : voList) {
            vo.setChildren(new ArrayList<>());
            idMap.put(vo.getId(), vo);
        }

        // 构建树形结构
        List<VideoDeviceGroupResultVO> roots = new ArrayList<>();
        for (VideoDeviceGroupResultVO vo : voList) {
            Long parentId = vo.getParentId();
            if (parentId == null || parentId == 0L) {
                // 顶层节点
                roots.add(vo);
            } else {
                VideoDeviceGroupResultVO parent = idMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 父节点不存在（可能被禁用或删除），作为顶层节点
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(VideoDeviceGroup entity) {
        computePathAndLevel(entity);
        superManager.save(entity);
        // save 后 id 已生成，回填 groupPath
        if (entity.getId() != null) {
            Long parentId = entity.getParentId();
            if (parentId == null || parentId == 0L) {
                entity.setGroupPath("/" + entity.getId());
            } else {
                entity.setGroupPath(entity.getGroupPath() + "/" + entity.getId());
            }
            superManager.updateById(entity);
        }
        log.info("保存分组: id={}, groupName={}, parentId={}, groupPath={}, groupLevel={}",
                entity.getId(), entity.getGroupName(), entity.getParentId(),
                entity.getGroupPath(), entity.getGroupLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(VideoDeviceGroup entity) {
        computePathAndLevel(entity);
        superManager.updateById(entity);
        log.info("更新分组: id={}, groupName={}, parentId={}, groupPath={}, groupLevel={}",
                entity.getId(), entity.getGroupName(), entity.getParentId(),
                entity.getGroupPath(), entity.getGroupLevel());
    }

    /**
     * 根据 parentId 自动计算 groupPath 和 groupLevel。
     * <ul>
     *   <li>顶级分组：groupLevel=1，groupPath=/id（save 后回填）</li>
     *   <li>子分组：groupLevel=父级+1，groupPath=父级path/id</li>
     * </ul>
     */
    private void computePathAndLevel(VideoDeviceGroup entity) {
        Long parentId = entity.getParentId();
        if (parentId == null || parentId == 0L) {
            entity.setParentId(null);
            entity.setGroupLevel(1);
            // 新建时 id 还没有，path 需要在 save 后回填
            // 这里先设置为空，save 后在 saveGroup 中补偿
        } else {
            VideoDeviceGroup parent = superManager.getById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("父分组不存在: parentId=" + parentId);
            }
            entity.setGroupLevel(parent.getGroupLevel() == null ? 2 : parent.getGroupLevel() + 1);
            String parentPath = parent.getGroupPath() != null ? parent.getGroupPath() : "/" + parent.getId();
            // 更新时 id 已有，新建时还没有，需要 save 后回填
            if (entity.getId() != null) {
                entity.setGroupPath(parentPath + "/" + entity.getId());
            } else {
                // 暂存父路径，save 后回填
                entity.setGroupPath(parentPath);
            }
        }
    }

}
