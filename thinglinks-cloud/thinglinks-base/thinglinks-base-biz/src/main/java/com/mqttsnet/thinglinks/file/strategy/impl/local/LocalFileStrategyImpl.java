package com.mqttsnet.thinglinks.file.strategy.impl.local;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.CollHelper;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.file.domain.FileDeleteBO;
import com.mqttsnet.thinglinks.file.domain.FileGetUrlBO;
import com.mqttsnet.thinglinks.file.entity.File;
import com.mqttsnet.thinglinks.file.enumeration.FileStorageType;
import com.mqttsnet.thinglinks.file.mapper.FileMapper;
import com.mqttsnet.thinglinks.file.properties.FileServerProperties;
import com.mqttsnet.thinglinks.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mqttsnet
 * @date 2020/11/22 5:00 下午
 */
@Slf4j
@DS(DsConstant.DEFAULTS)
@Component("LOCAL")
@Primary
public class LocalFileStrategyImpl extends AbstractFileStrategy {
    public LocalFileStrategyImpl(FileServerProperties fileProperties, FileMapper fileMapper) {
        super(fileProperties, fileMapper);
    }

    @Override
    protected void uploadFile(File file, MultipartFile multipartFile, String bucket) throws Exception {
        FileServerProperties.Local local = fileProperties.getLocal();
        bucket = StrUtil.isEmpty(bucket) ? local.getBucket() : bucket;
        bucket = requireSafePathSegment(bucket, "存储桶");
        String bizType = requireSafePathSegment(file.getBizType(), "业务类型");

        //生成文件名
        String uniqueFileName = getUniqueFileName(file);
        // 相对路径
        String path = getPath(bizType, uniqueFileName);
        // web服务器存放的绝对路径
        Path absolutePath = resolveUploadTarget(local, bucket, path);

        // 存储文件
        java.io.File outFile = absolutePath.toFile();
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), outFile);

        // 返回数据
        String url = local.getUrlPrefix() + bucket + StrPool.SLASH + path;
        file.setUrl(url);
        file.setUniqueFileName(uniqueFileName);
        file.setPath(path);
        file.setBucket(bucket);
        file.setStorageType(FileStorageType.LOCAL);
    }

    @Override
    public boolean delete(FileDeleteBO file) {
        FileServerProperties.Local local = fileProperties.getLocal();
        String bucket = requireSafePathSegment(file.getBucket(), "存储桶");
        try {
            Path target = resolveDeleteTarget(local, bucket, file.getPath());
            if (target == null) {
                return true;
            }
            if (Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS)) {
                throw BizException.wrap("文件删除路径无效");
            }
            Files.deleteIfExists(target);
            return true;
        } catch (IOException e) {
            throw new BizException("文件删除失败", e);
        }
    }

    private Path resolveUploadTarget(FileServerProperties.Local local, String bucket, String relativePath)
            throws IOException {
        Path storageRoot = prepareStorageRoot(local, true);
        Path bucketRoot = prepareDirectory(storageRoot, bucket, true);
        Path relative = requireSafeRelativePath(relativePath);
        Path parent = bucketRoot;
        for (int i = 0; i < relative.getNameCount() - 1; i++) {
            parent = prepareDirectory(parent, relative.getName(i).toString(), true);
        }
        Path target = parent.resolve(relative.getFileName()).normalize();
        if (!target.startsWith(bucketRoot)
                || Files.isSymbolicLink(target)
                || Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS)) {
            throw BizException.wrap("文件存储路径无效");
        }
        return target;
    }

    private Path resolveDeleteTarget(FileServerProperties.Local local, String bucket, String relativePath)
            throws IOException {
        Path relative = requireSafeRelativePath(relativePath);
        Path storageRoot = prepareStorageRoot(local, false);
        if (storageRoot == null) {
            return null;
        }
        Path bucketRoot = prepareDirectory(storageRoot, bucket, false);
        if (bucketRoot == null) {
            return null;
        }
        Path parent = bucketRoot;
        for (int i = 0; i < relative.getNameCount() - 1; i++) {
            parent = prepareDirectory(parent, relative.getName(i).toString(), false);
            if (parent == null) {
                return null;
            }
        }
        Path target = parent.resolve(relative.getFileName()).normalize();
        if (target.equals(bucketRoot) || !target.startsWith(bucketRoot)) {
            throw BizException.wrap("文件删除路径无效");
        }
        return target;
    }

    private Path prepareStorageRoot(FileServerProperties.Local local, boolean create) throws IOException {
        Path configuredRoot = Paths.get(local.getStoragePath()).toAbsolutePath().normalize();
        if (!Files.exists(configuredRoot, LinkOption.NOFOLLOW_LINKS)) {
            if (!create) {
                return null;
            }
            Files.createDirectories(configuredRoot);
        }
        if (!Files.isDirectory(configuredRoot)) {
            throw BizException.wrap("文件存储根目录无效");
        }
        return configuredRoot.toRealPath();
    }

    private Path prepareDirectory(Path parent, String child, boolean create) throws IOException {
        Path directory = parent.resolve(child).normalize();
        if (!directory.startsWith(parent) || Files.isSymbolicLink(directory)) {
            throw BizException.wrap("文件存储路径无效");
        }
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (!create) {
                return null;
            }
            Files.createDirectory(directory);
        }
        if (!Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
            throw BizException.wrap("文件存储路径无效");
        }
        return directory;
    }

    private Path requireSafeRelativePath(String relativePath) {
        if (StrUtil.isBlank(relativePath)) {
            throw BizException.wrap("文件相对路径无效");
        }
        Path relative = Paths.get(relativePath).normalize();
        if (relative.isAbsolute()
                || StrUtil.isEmpty(relative.toString())
                || relative.startsWith("..")) {
            throw BizException.wrap("文件相对路径无效");
        }
        return relative;
    }

    private String requireSafePathSegment(String value, String fieldName) {
        String segment = StrUtil.trim(value);
        if (StrUtil.isEmpty(segment) || !segment.matches("[\\p{L}\\p{N}][\\p{L}\\p{N}._-]{0,127}")) {
            throw BizException.wrap(fieldName + "格式不正确");
        }
        return segment;
    }

    @Override
    public Map<String, String> findUrl(List<FileGetUrlBO> fileGets) {
        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(fileGets.size()));
        // 方式1 取上传时存的url （多查询一次数据库）
        /*
        List<String> paths = fileGets.stream().map(FileGetUrlBO::getPath).collect(Collectors.toList());
        List<File> list = fileMapper.selectList(Wraps.<File>lbQ().eq(File::getPath, paths));
        list.forEach(item -> map.put(item.getPath(), item.getUrl()));
        */

        // 方式2 重新拼接 （urlPrefix 可能跟上传时不一样）
        FileServerProperties.Local local = fileProperties.getLocal();
        fileGets.forEach(item -> {
            String url = local.getUrlPrefix() +
                         item.getBucket() +
                         StrPool.SLASH +
                         item.getPath();
            map.put(item.getPath(), url);
        });
        return map;
    }
}
