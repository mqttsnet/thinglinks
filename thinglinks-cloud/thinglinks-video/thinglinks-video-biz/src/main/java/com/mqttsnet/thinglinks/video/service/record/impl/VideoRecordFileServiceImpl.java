package com.mqttsnet.thinglinks.video.service.record.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.file.facade.FileFacade;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordPlan;
import com.mqttsnet.thinglinks.video.manager.record.VideoRecordFileManager;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * 录像文件业务实现。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoRecordFileServiceImpl extends SuperServiceImpl<VideoRecordFileManager, Long, VideoRecordFile> implements VideoRecordFileService {

    private final FileFacade fileFacade;
    private final VideoRecordPlanService videoRecordPlanService;

    @Override
    public List<VideoRecordFile> listByDeviceAndChannel(String deviceIdentification, String channelIdentification) {
        return superManager.listByDeviceAndChannel(deviceIdentification, channelIdentification);
    }

    @Override
    public List<VideoRecordFile> listByPlanId(Long planId) {
        return superManager.listByPlanId(planId);
    }

    @Override
    public void saveRecordFile(VideoRecordFile recordFile) {
        superManager.save(recordFile);
        log.info("保存录像文件: fileName={}, device={}, channel={}",
                recordFile.getFileName(), recordFile.getDeviceIdentification(), recordFile.getChannelIdentification());
    }

    @Override
    public List<VideoRecordFile> listByDate(String deviceIdentification, String channelIdentification, LocalDate date) {
        return superManager.listByDate(deviceIdentification, channelIdentification, date);
    }

    @Override
    public List<LocalDate> getRecordDates(String deviceIdentification, String channelIdentification, YearMonth month) {
        return superManager.getRecordDates(deviceIdentification, channelIdentification, month);
    }

    @Override
    public String getFileUrl(Long recordFileId) {
        VideoRecordFile recordFile = superManager.getById(recordFileId);
        if (recordFile == null || recordFile.getFileId() == null) {
            log.warn("录像文件不存在或无关联文件: recordFileId={}", recordFileId);
            return null;
        }

        Long fileId = recordFile.getFileId();
        try {
            R<Map<Long, String>> result = fileFacade.findUrlFromDefById(Collections.singletonList(fileId));
            if (result != null && result.getIsSuccess() && result.getData() != null) {
                String url = result.getData().get(fileId);
                if (StrUtil.isNotEmpty(url)) {
                    log.debug("获取录像文件URL成功: recordFileId={}, fileId={}, url={}", recordFileId, fileId, url);
                    return url;
                }
            }
            log.warn("未获取到录像文件URL: recordFileId={}, fileId={}", recordFileId, fileId);
            return null;
        } catch (Exception e) {
            log.error("调用 FileFacade 获取录像文件URL异常: recordFileId={}, fileId={}", recordFileId, fileId, e);
            return null;
        }
    }

    @Override
    public int cleanExpiredFiles(Long tenantId) {
        ContextUtil.setTenantId(tenantId);
        List<VideoRecordPlan> activePlans = videoRecordPlanService.listActivePlans();
        int totalCleaned = 0;

        for (VideoRecordPlan plan : activePlans) {
            if (plan.getRetentionDays() == null || plan.getRetentionDays() <= 0) {
                continue;
            }

            LocalDateTime expireTime = LocalDateTime.now().minusDays(plan.getRetentionDays());
            List<VideoRecordFile> files = superManager.listByPlanId(plan.getId());

            for (VideoRecordFile file : files) {
                if (file.getCreatedTime() != null && file.getCreatedTime().isBefore(expireTime) && file.getFileStatus() != 2) {
                    file.setFileStatus(2); // 标记为已过期
                    superManager.updateById(file);
                    log.info("录像文件已标记过期: fileId={}, fileName={}, planId={}", file.getId(), file.getFileName(), plan.getId());
                    totalCleaned++;
                }
            }
        }

        log.info("过期录像文件清理完成: tenantId={}, cleanedCount={}", tenantId, totalCleaned);
        return totalCleaned;
    }
}
