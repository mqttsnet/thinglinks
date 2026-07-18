package com.mqttsnet.thinglinks.file.controller;

import com.mqttsnet.thinglinks.file.enumeration.FileStorageType;
import com.mqttsnet.thinglinks.file.service.FileService;
import com.mqttsnet.thinglinks.file.vo.param.FileUploadVO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileAnyoneControllerTest {

    @Test
    void anonymousUploadUsesConfiguredStorageAndBucket() {
        FileService fileService = mock(FileService.class);
        FileAnyoneController controller = new FileAnyoneController(fileService);
        MockMultipartFile file = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());
        FileUploadVO request = new FileUploadVO();
        request.setBizType("avatar");
        request.setBucket("../../outside");
        request.setStorageType(FileStorageType.LOCAL);

        controller.upload(file, request);

        var uploadCaptor = org.mockito.ArgumentCaptor.forClass(FileUploadVO.class);
        verify(fileService).upload(same(file), uploadCaptor.capture());
        assertEquals("avatar", uploadCaptor.getValue().getBizType());
        assertNull(uploadCaptor.getValue().getBucket());
        assertNull(uploadCaptor.getValue().getStorageType());
    }

    @Test
    void anonymousUploadRejectsUnsafeBusinessTypeDuringBinding() throws Exception {
        FileService fileService = mock(FileService.class);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new FileAnyoneController(fileService))
                .build();
        MockMultipartFile file = new MockMultipartFile(
                "file", "example.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/anyone/file/upload")
                        .file(file)
                        .param("bizType", "unsafe?type"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(fileService);
    }
}
