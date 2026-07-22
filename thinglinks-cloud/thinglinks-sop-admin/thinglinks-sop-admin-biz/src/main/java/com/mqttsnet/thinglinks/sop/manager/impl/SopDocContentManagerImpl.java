package com.mqttsnet.thinglinks.sop.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.thinglinks.sop.entity.SopDocContent;
import com.mqttsnet.thinglinks.sop.manager.SopDocContentManager;
import com.mqttsnet.thinglinks.sop.mapper.SopDocContentMapper;

/**
 * <p>
 * 通用业务实现类
 * 文档内容
 * </p>
 *
 * @author zuihou
 * @since 2025-05-07 10:52:47
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SopDocContentManagerImpl extends SuperManagerImpl<SopDocContentMapper, SopDocContent> implements SopDocContentManager {

    /**
     * 新增或更新文档内容(按 docInfoId upsert).
     *
     * <p><b>事务边界由调用方 Service 控制</b> ── 不在 Manager 层再开事务:
     * 当前唯一调用方 {@code SopDocInfoServiceImpl.syncContent → syncToCustom} 已用
     * {@code @Transactional(rollbackFor = Exception.class)} 包住外层,Spring 默认 PROPAGATION.REQUIRED
     * 会让本方法的 save/update 自动加入外层事务.若 Manager 再标 @Transactional 是冗余嵌套.</p>
     *
     * @param docInfoId 文档 ID
     * @param content   文档内容
     */
    public void saveContent(Long docInfoId, String content) {
        SopDocContent docContent = this.getOne(Wraps.<SopDocContent>lbQ().eq(SopDocContent::getDocInfoId, docInfoId));
        boolean save = false;
        if (docContent == null) {
            save = true;
            docContent = new SopDocContent();
        }
        docContent.setDocInfoId(docInfoId);
        docContent.setContent(content);
        if (save) {
            this.save(docContent);
        } else {
            this.updateById(docContent);
        }
    }


}


