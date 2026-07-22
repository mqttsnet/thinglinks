package com.mqttsnet.thinglinks.file.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.file.entity.File;
import com.mqttsnet.thinglinks.file.manager.FileManager;
import com.mqttsnet.thinglinks.file.properties.FileServerProperties;
import com.mqttsnet.thinglinks.file.service.FileService;
import com.mqttsnet.thinglinks.file.strategy.FileContext;
import com.mqttsnet.thinglinks.file.vo.param.FileUploadVO;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 业务实现类
 * 增量文件上传日志
 * </p>
 *
 * @author mqttsnet
 * @date 2021-06-30
 * @create [2021-06-30] [mqttsnet] [初始创建]
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@DS(DsConstant.DEFAULTS)
public class FileServiceImpl extends SuperServiceImpl<FileManager, Long, File> implements FileService {
    @Resource
    private FileContext fileContext;
    @Resource
    private FileManager fileManager;
    @Resource
    private FileServerProperties fileServerProperties;

    @Override
    public List<FileResultVO> listByBizIdAndBizType(Long bizId, String bizType) {
        ArgumentAssert.notNull(bizId, "请传入业务id");
        return superManager.listByBizIdAndBizType(bizId, bizType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResultVO upload(MultipartFile file, FileUploadVO fileUploadVO) {
        long methodStart = System.currentTimeMillis();
        // 忽略路径字段,只处理文件类型
        if (file.isEmpty()) {
            throw new BizException("请上传有效文件");
        }
        if (fileServerProperties.getMaxUploadSize() == null
                || fileServerProperties.getMaxUploadSize().toBytes() <= 0) {
            throw new BizException("请配置有效的文件上传容量上限");
        }
        if (file.getSize() > fileServerProperties.getMaxUploadSize().toBytes()) {
            throw new BizException("文件大小超过上传容量上限");
        }
        if (fileUploadVO == null
                || StrUtil.isBlank(fileUploadVO.getBizType())
                || !fileUploadVO.getBizType().matches(FileUploadVO.BIZ_TYPE_PATTERN)) {
            throw new BizException("业务类型格式不正确");
        }

        if (!fileServerProperties.validSuffix(file.getOriginalFilename())) {
            throw new BizException("文件后缀不支持");
        }
        if (StrUtil.containsAny(file.getOriginalFilename(), "../", "./")) {
            throw new BizException("文件名不能含有特殊字符");
        }
        long start = System.currentTimeMillis();
        File fileFile = fileContext.upload(file, fileUploadVO);
        long uploadEnd = System.currentTimeMillis();
        fileFile.setTenantId(ContextUtil.getTenantId());
        fileManager.save(fileFile);
        long saveEnd = System.currentTimeMillis();

        log.info("耗时统计：{}, {}, {}", (methodStart - start), (uploadEnd - start), (saveEnd - start));

        return BeanPlusUtil.toBean(fileFile, FileResultVO.class);
    }

    @Override
    public Map<String, String> findUrlByPath(List<String> paths) {
        if (CollUtil.isEmpty(paths)) {
            return Collections.emptyMap();
        }
        return fileContext.findUrlByPath(paths);
    }

    @Override
    public Map<Long, String> findUrlById(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return fileContext.findUrlById(ids);
    }

    @Override
    public Map<Long, FileResultVO> findByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return fileContext.findByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        List<File> list = list(Wrappers.<File>lambdaQuery().in(File::getId, ids));
        if (list.isEmpty()) {
            return false;
        }
        fileManager.removeByIds(ids);
        return fileContext.delete(list);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, List<Long> ids) throws Exception {
        List<File> list = fileManager.listByIds(ids);
        ArgumentAssert.notEmpty(list, "请配置正确的文件存储类型");

        fileContext.download(request, response, list);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, Long id) throws Exception {
        File file = fileManager.getById(id);
        ArgumentAssert.notNull(file, "请配置正确的文件存储类型");
        fileContext.download(request, response, file);
    }

}
