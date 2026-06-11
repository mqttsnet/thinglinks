package com.mqttsnet.thinglinks.common.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传工具类。
 * <p>
 * 提供 File / byte[] / InputStream → MultipartFile 的转换，以及 MD5、SHA256 计算等工具方法。
 * <p>
 * 内置轻量级 {@link ByteArrayMultipartFile} 实现，替代 spring-test 的 MockMultipartFile，
 * 可在生产代码中安全使用。
 *
 * @author mqttsnet
 * @version 1.1.0
 * @since 2025/7/2
 */
public class FileUploadUtils {

    private static final Tika TIKA = new Tika();
    /**
     * 内存阈值 10MB：超过此大小的文件优先使用流方式处理
     */
    private static final long MAX_MEMORY_SIZE = 10 * 1024 * 1024;

    private FileUploadUtils() {
    }

    // ─── MultipartFile 构建 ─────────────────────────────────────

    /**
     * File → MultipartFile（自动检测 MIME 类型，自动内存优化）
     *
     * @param file 源文件
     * @return MultipartFile 实例
     */
    public static MultipartFile toMultipartFile(File file) throws IOException {
        String mimeType = TIKA.detect(file.toPath());
        byte[] content = file.length() <= MAX_MEMORY_SIZE
            ? FileUtil.readBytes(file)
            : IoUtil.readBytes(FileUtil.getInputStream(file));
        return new ByteArrayMultipartFile(file.getName(), file.getName(), mimeType, content);
    }

    /**
     * byte[] → MultipartFile
     *
     * @param fileName    原始文件名（含扩展名）
     * @param contentType MIME 类型，如 "video/mp4"，可传 null 自动推断
     * @param content     文件内容字节数组
     * @return MultipartFile 实例
     */
    public static MultipartFile toMultipartFile(String fileName, String contentType, byte[] content) {
        if (StrUtil.isBlank(contentType)) {
            contentType = TIKA.detect(content, fileName);
        }
        return new ByteArrayMultipartFile("file", fileName, contentType, content);
    }

    /**
     * InputStream → MultipartFile（读取流到内存后构建）
     *
     * @param fileName    原始文件名（含扩展名）
     * @param contentType MIME 类型，可传 null 自动推断
     * @param inputStream 输入流（方法内部会读取并关闭）
     * @return MultipartFile 实例
     */
    public static MultipartFile toMultipartFile(String fileName, String contentType, InputStream inputStream) throws IOException {
        byte[] content = IoUtil.readBytes(inputStream);
        return toMultipartFile(fileName, contentType, content);
    }

    // ─── 文件操作 ───────────────────────────────────────────────

    /**
     * MultipartFile → File（自动创建父目录）
     */
    public static File saveToFile(MultipartFile file, Path targetDir) throws IOException {
        String filename = StrUtil.uuid() + "." + FileUtil.extName(file.getOriginalFilename());
        Path targetPath = targetDir.resolve(filename);
        FileUtil.mkdir(targetDir.toFile());
        FileUtil.writeFromStream(file.getInputStream(), targetPath.toFile());
        return targetPath.toFile();
    }

    /**
     * 创建临时文件
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return FileUtil.createTempFile(prefix, suffix, true);
    }

    /**
     * 安全获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        return StrUtil.blankToDefault(FileUtil.extName(filename), "");
    }

    // ─── 哈希计算 ───────────────────────────────────────────────

    /**
     * 计算 MultipartFile 的 MD5（自动内存优化）
     */
    public static String getMd5(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (multipartFile.getSize() <= MAX_MEMORY_SIZE) {
            return DigestUtil.md5Hex(multipartFile.getBytes());
        } else {
            try (InputStream inputStream = multipartFile.getInputStream()) {
                return DigestUtil.md5Hex(inputStream);
            }
        }
    }

    /**
     * 计算 File 的 MD5
     */
    public static String getMd5(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        return DigestUtil.md5Hex(file);
    }

    /**
     * 计算 MultipartFile 的 SHA256（自动内存优化）
     */
    public static String getSha256(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (multipartFile.getSize() <= MAX_MEMORY_SIZE) {
            return DigestUtil.sha256Hex(multipartFile.getBytes());
        } else {
            try (InputStream inputStream = multipartFile.getInputStream()) {
                return DigestUtil.sha256Hex(inputStream);
            }
        }
    }

    /**
     * 计算 File 的 SHA256
     */
    public static String getSha256(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        return DigestUtil.sha256Hex(file);
    }

    // ─── 内置轻量级 MultipartFile 实现 ──────────────────────────

    /**
     * 基于字节数组的 MultipartFile 实现。
     * <p>
     * 替代 spring-test 的 MockMultipartFile（test scope 不可用于生产代码），
     * 适用于程序内部构建 MultipartFile 进行跨服务上传等场景。
     */
    public static class ByteArrayMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content != null ? content : new byte[0];
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            FileUtil.writeBytes(content, dest);
        }
    }
}
