package com.mqttsnet.thinglinks.file.service.impl;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.file.properties.FileServerProperties;
import com.mqttsnet.thinglinks.file.vo.param.FileUploadVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FileServiceImplTest {

    @Test
    void uploadRejectsFileLargerThanConfiguredLimit() {
        FileServerProperties properties = new FileServerProperties();
        properties.setMaxUploadSize(DataSize.ofBytes(4));
        FileServiceImpl service = new FileServiceImpl();
        ReflectionTestUtils.setField(service, "fileServerProperties", properties);
        MockMultipartFile file = new MockMultipartFile(
                "file", "example.txt", "text/plain", "12345".getBytes());
        FileUploadVO request = new FileUploadVO();
        request.setBizType("avatar");

        assertThrows(BizException.class, () -> service.upload(file, request));
    }

    @Test
    void uploadRejectsUnsafeBusinessTypeForEveryStorageStrategy() {
        FileServerProperties properties = new FileServerProperties();
        properties.setMaxUploadSize(DataSize.ofMegabytes(1));
        properties.setSuffix("txt");
        FileServiceImpl service = new FileServiceImpl();
        ReflectionTestUtils.setField(service, "fileServerProperties", properties);
        MockMultipartFile file = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());
        FileUploadVO request = new FileUploadVO();
        request.setBizType("unsafe?type");

        assertThrows(BizException.class, () -> service.upload(file, request));
    }
}
