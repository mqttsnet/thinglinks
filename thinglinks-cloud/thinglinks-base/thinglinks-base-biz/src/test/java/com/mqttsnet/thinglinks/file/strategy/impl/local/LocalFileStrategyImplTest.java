package com.mqttsnet.thinglinks.file.strategy.impl.local;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.file.domain.FileDeleteBO;
import com.mqttsnet.thinglinks.file.entity.File;
import com.mqttsnet.thinglinks.file.mapper.FileMapper;
import com.mqttsnet.thinglinks.file.properties.FileServerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class LocalFileStrategyImplTest {

    @TempDir
    Path storageRoot;

    private FileServerProperties properties;
    private LocalFileStrategyImpl strategy;

    @BeforeEach
    void setUp() {
        ContextUtil.setTenantId(1L);
        properties = new FileServerProperties();
        properties.getLocal().setStoragePath(storageRoot.toString());
        properties.getLocal().setBucket("public");
        strategy = new LocalFileStrategyImpl(properties, mock(FileMapper.class));
    }

    @AfterEach
    void tearDown() {
        ContextUtil.remove();
    }

    @Test
    void uploadStoresFileInsideConfiguredRoot() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());

        File stored = strategy.upload(multipartFile, "public", "avatar");

        Path actualPath = storageRoot.resolve(stored.getBucket()).resolve(stored.getPath()).normalize();
        assertTrue(actualPath.startsWith(storageRoot.toAbsolutePath().normalize()));
        assertTrue(Files.exists(actualPath));
        assertEquals("public", stored.getBucket());
    }

    @Test
    void uploadRejectsTraversalInBucket() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());

        assertThrows(BizException.class,
                () -> strategy.upload(multipartFile, "../outside", "avatar"));
        assertFalse(Files.exists(storageRoot.getParent().resolve("outside")));
    }

    @Test
    void uploadRejectsTraversalInBusinessType() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());

        assertThrows(BizException.class,
                () -> strategy.upload(multipartFile, "public", "../outside"));
    }

    @Test
    void uploadRejectsBucketSymlinkOutsideStorageRoot() throws Exception {
        Path outside = Files.createTempDirectory(storageRoot.getParent(), "outside-");
        Path link = Files.createSymbolicLink(storageRoot.resolve("public"), outside);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());

        try {
            assertThrows(BizException.class,
                    () -> strategy.upload(multipartFile, "public", "avatar"));
        } finally {
            Files.deleteIfExists(link);
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void deleteRejectsStoredPathOutsideBucketRoot() {
        FileDeleteBO delete = FileDeleteBO.builder()
                .bucket("public")
                .path("../../outside.txt")
                .build();

        assertThrows(BizException.class, () -> strategy.delete(delete));
    }

    @Test
    void deleteRejectsBucketRootPath() throws Exception {
        Files.createDirectory(storageRoot.resolve("public"));
        FileDeleteBO delete = FileDeleteBO.builder()
                .bucket("public")
                .path(".")
                .build();

        assertThrows(BizException.class, () -> strategy.delete(delete));
        assertTrue(Files.isDirectory(storageRoot.resolve("public")));
    }

    @Test
    void deleteRemovesSymlinkWithoutDeletingItsTarget() throws Exception {
        Path bucket = Files.createDirectory(storageRoot.resolve("public"));
        Path target = Files.createTempFile(storageRoot.getParent(), "outside-", ".txt");
        Path link = Files.createSymbolicLink(bucket.resolve("linked.txt"), target);
        FileDeleteBO delete = FileDeleteBO.builder()
                .bucket("public")
                .path("linked.txt")
                .build();

        assertTrue(strategy.delete(delete));
        assertFalse(Files.exists(link, java.nio.file.LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.exists(target));
        Files.deleteIfExists(target);
    }
}
