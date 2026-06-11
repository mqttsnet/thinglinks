package com.mqttsnet.thinglinks.video.listener;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.mqttsnet.basic.cache.lock.DistributedLock;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.lock.video.VideoLockKeyBuilder;
import com.mqttsnet.thinglinks.common.utils.FileUploadUtils;
import com.mqttsnet.thinglinks.context.ContextAwareExecutor;
import com.mqttsnet.thinglinks.video.utils.MediaUrlUtils;
import com.mqttsnet.thinglinks.file.enumeration.FileBizType;
import com.mqttsnet.thinglinks.file.facade.FileFacade;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import com.mqttsnet.thinglinks.video.dto.media.VideoMediaServerResultDTO;
import com.mqttsnet.thinglinks.video.dto.media.zlm.RecordInfo;
import com.mqttsnet.thinglinks.video.entity.record.VideoRecordFile;
import com.mqttsnet.thinglinks.video.media.server.event.media.MediaRecordMp4Event;
import com.mqttsnet.thinglinks.video.service.record.VideoRecordFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 录像文件生成事件监听器。
 * <p>
 * 监听 ZLMediaKit {@code on_record_mp4} Hook 触发的 {@link MediaRecordMp4Event}，
 * 完成以下流程：
 * <ol>
 *   <li>从 ZLM 服务器下载录像文件（通过 ZLM 返回的 URL）</li>
 *   <li>通过 {@link FileFacade#upload} 上传到 base 通用 OSS 存储</li>
 *   <li>获取返回的 fileId</li>
 *   <li>创建 {@link VideoRecordFile} 记录并持久化到数据库</li>
 * </ol>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-05
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MediaRecordMp4EventListener {

    private final FileFacade fileFacade;
    private final VideoRecordFileService videoRecordFileService;
    private final DistributedLock distributedLock;
    private final ContextAwareExecutor contextAwareExecutor;
    @Qualifier("videoDefaultExecutor") private final ThreadPoolExecutor videoDefaultExecutor;

    /**
     * 处理录像文件生成事件。
     * <p>
     * 异步执行，避免阻塞 ZLM Hook 回调线程。
     */
    @EventListener
    public void onRecordMp4(MediaRecordMp4Event event) {
        contextAwareExecutor.executeWithContext(() -> {
            doProcessRecord(event);
            return null;
        }, videoDefaultExecutor);
    }

    private void doProcessRecord(MediaRecordMp4Event event) {
        RecordInfo recordInfo = event.getRecordInfo();
        VideoMediaServerResultDTO mediaServer = event.getMediaServer();
        String app = event.getApp();
        String stream = event.getStream();
        String fileName = recordInfo.getFileName();

        log.info("录像文件事件: app={}, stream={}, fileName={}, fileSize={}, timeLenSec={}",
            app, stream, fileName, recordInfo.getFileSize(), recordInfo.getTimeLen());

        // 分布式锁去重，防止集群环境下同一文件被重复处理
        CacheKey lockKey = VideoLockKeyBuilder.forRecordFileSave(fileName);
        var lockResult = distributedLock.tryLockAndRun(
                lockKey.getKey(), lockKey.getExpire().getSeconds(), TimeUnit.SECONDS,
                () -> {
                    Long fileId = null;
                    try {
                        fileId = uploadRecordFileToOss(recordInfo, mediaServer);
                    } catch (Exception e) {
                        log.error("录像文件上传 OSS 失败: fileName={}, error={}", fileName, e.getMessage(), e);
                    }
                    // 无论上传是否成功，都保存录像文件元数据记录
                    saveRecordFileMetadata(event, recordInfo, mediaServer, fileId);
                    return null;
                });
        if (!lockResult.isLocked()) {
            log.debug("录像文件正在被其他实例处理: fileName={}", fileName);
        }
    }

    /**
     * 从 ZLM 下载录像文件并上传到 base OSS。
     *
     * @param recordInfo  录像文件信息
     * @param mediaServer 媒体服务器信息
     * @return 上传后的 fileId，上传失败返回 null
     */
    private Long uploadRecordFileToOss(RecordInfo recordInfo, VideoMediaServerResultDTO mediaServer) throws IOException {
        String downloadUrl = buildFileDownloadUrl(recordInfo, mediaServer);
        String fileName = recordInfo.getFileName();

        log.info("开始下载录像文件: url={}, fileName={}", downloadUrl, fileName);
        // 使用 Hutool HttpUtil 下载文件字节数组（内置超时：连接10s，读取60s）
        byte[] fileBytes = HttpUtil.createGet(downloadUrl)
            .timeout(60000)
            .execute()
            .bodyBytes();
        // 通过 FileUploadUtils 构建 MultipartFile（contentType 传 null 自动推断）
        MultipartFile multipartFile = FileUploadUtils.toMultipartFile(fileName, null, fileBytes);

        FileResultVO result = fileFacade.upload(multipartFile, FileBizType.VIDEO_RECORD.getValue(), "", null);
        if (result != null && result.getId() != null) {
            log.info("录像文件上传 OSS 成功: fileName={}, fileId={}", fileName, result.getId());
            return result.getId();
        } else {
            log.warn("录像文件上传 OSS 返回空结果: fileName={}", fileName);
            return null;
        }
    }

    /**
     * 构建 ZLM 录像文件的 HTTP 下载地址。
     * <p>
     * 使用 {@link MediaUrlUtils#buildFileUrl} 统一处理流媒体服务器地址拼接。
     */
    private String buildFileDownloadUrl(RecordInfo recordInfo, VideoMediaServerResultDTO mediaServer) {
        return MediaUrlUtils.buildFileUrl(mediaServer.getHost(), mediaServer.getHttpPort(), recordInfo.getUrl());
    }

    /**
     * 保存录像文件元数据到数据库。
     */
    private void saveRecordFileMetadata(MediaRecordMp4Event event, RecordInfo recordInfo,
                                        VideoMediaServerResultDTO mediaServer, Long fileId) {
        try {
            LocalDateTime startTime = Instant.ofEpochSecond(recordInfo.getStartTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

            int durationSec = (int) Math.ceil(recordInfo.getTimeLen());
            LocalDateTime endTime = startTime.plusSeconds(durationSec);

            String fileName = recordInfo.getFileName();
            String fileFormat = StrUtil.blankToDefault(FileUtil.extName(fileName), "");

            VideoRecordFile recordFile = VideoRecordFile.builder()
                .planId(event.getPlanId())
                .deviceIdentification(null)
                .channelIdentification(null)
                .streamIdentification(event.getStream())
                .app(event.getApp())
                .mediaIdentification(mediaServer.getMediaIdentification())
                .fileName(fileName)
                .fileId(fileId)
                .fileSize(recordInfo.getFileSize())
                .fileFormat(fileFormat)
                .duration(durationSec)
                .startTime(startTime)
                .endTime(endTime)
                .fileStatus(fileId != null ? 1 : 0)
                .build();

            videoRecordFileService.saveRecordFile(recordFile);
            log.info("录像文件元数据保存成功: fileName={}, fileId={}, stream={}", fileName, fileId, event.getStream());
        } catch (Exception e) {
            log.error("保存录像文件元数据失败: fileName={}", recordInfo.getFileName(), e);
        }
    }

}
