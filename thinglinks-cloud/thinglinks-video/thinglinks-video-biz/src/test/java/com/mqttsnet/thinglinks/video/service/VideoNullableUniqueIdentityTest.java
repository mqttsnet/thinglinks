package com.mqttsnet.thinglinks.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.mqttsnet.thinglinks.video.entity.gateway.VideoGatewayMapping;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroupRelation;
import com.mqttsnet.thinglinks.video.manager.gateway.VideoGatewayMappingManager;
import com.mqttsnet.thinglinks.video.service.gateway.impl.VideoGatewayMappingServiceImpl;
import com.mqttsnet.thinglinks.video.service.group.impl.VideoDeviceGroupRelationServiceImpl;
import com.mqttsnet.thinglinks.video.vo.save.gateway.VideoGatewayMappingSaveVO;
import com.mqttsnet.thinglinks.video.vo.save.group.VideoDeviceGroupRelationSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.gateway.VideoGatewayMappingUpdateVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class VideoNullableUniqueIdentityTest {

    @Test
    void blankOptionalChannelIdentifiersBecomeNullBeforeInsert() {
        VideoDeviceGroupRelation groupRelation = new TestGroupRelationService().mapSave(
                VideoDeviceGroupRelationSaveVO.builder().channelIdentification("   ").build());
        VideoGatewayMapping gatewayMapping = new TestGatewayMappingService().mapSave(
                VideoGatewayMappingSaveVO.builder().srcChannelIdentification("  ").build());

        assertThat(groupRelation.getChannelIdentification()).isNull();
        assertThat(gatewayMapping.getSrcChannelIdentification()).isNull();
    }

    @Test
    void optionalChannelIdentifiersAreTrimmedBeforeInsertAndUpdate() {
        VideoDeviceGroupRelation groupRelation = new TestGroupRelationService().mapSave(
                VideoDeviceGroupRelationSaveVO.builder()
                        .deviceIdentification(" device-1 ")
                        .channelIdentification(" channel-1 ")
                        .build());
        TestGatewayMappingService gatewayService = new TestGatewayMappingService();
        VideoGatewayMapping savedGatewayMapping = gatewayService.mapSave(
                VideoGatewayMappingSaveVO.builder()
                        .srcProtocol(" JT1078 ")
                        .srcDeviceIdentification(" source-device-1 ")
                        .srcChannelIdentification(" source-channel-1 ")
                        .build());
        VideoGatewayMapping updatedGatewayMapping = gatewayService.mapUpdate(
                VideoGatewayMappingUpdateVO.builder().srcChannelIdentification(" source-channel-2 ").build());

        assertThat(groupRelation.getChannelIdentification()).isEqualTo("channel-1");
        assertThat(groupRelation.getDeviceIdentification()).isEqualTo("device-1");
        assertThat(savedGatewayMapping.getSrcProtocol()).isEqualTo("JT1078");
        assertThat(savedGatewayMapping.getSrcDeviceIdentification()).isEqualTo("source-device-1");
        assertThat(savedGatewayMapping.getSrcChannelIdentification()).isEqualTo("source-channel-1");
        assertThat(updatedGatewayMapping.getSrcChannelIdentification()).isEqualTo("source-channel-2");
    }

    @Test
    void nonSpaceCharactersArePreservedByTheDatabaseCompatibleNormalizer() {
        VideoDeviceGroupRelation groupRelation = new TestGroupRelationService().mapSave(
                VideoDeviceGroupRelationSaveVO.builder().channelIdentification(" \t ").build());
        VideoGatewayMapping gatewayMapping = new TestGatewayMappingService().mapSave(
                VideoGatewayMappingSaveVO.builder().srcChannelIdentification("　").build());

        assertThat(groupRelation.getChannelIdentification()).isEqualTo("\t");
        assertThat(gatewayMapping.getSrcChannelIdentification()).isEqualTo("　");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void blankGatewayChannelIsExplicitlyPersistedAsNull() {
        VideoGatewayMappingManager manager = mock(VideoGatewayMappingManager.class);
        when(manager.updateById(any(VideoGatewayMapping.class))).thenReturn(true);
        when(manager.update(any(Wrapper.class))).thenReturn(true);
        TestGatewayMappingService service = new TestGatewayMappingService(manager);

        VideoGatewayMapping updated = service.updateById(
                VideoGatewayMappingUpdateVO.builder().id(9L).srcChannelIdentification("   ").build());

        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(manager).updateById(any(VideoGatewayMapping.class));
        verify(manager).update(wrapperCaptor.capture());
        UpdateWrapper<VideoGatewayMapping> wrapper =
                (UpdateWrapper<VideoGatewayMapping>) wrapperCaptor.getValue();
        assertThat(updated.getSrcChannelIdentification()).isNull();
        assertThat(wrapper.getSqlSet()).contains("src_channel_identification");
        assertThat(wrapper.getSqlSegment()).contains("id").contains("deleted");
        assertThat(wrapper.getParamNameValuePairs()).containsValue(null);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void omittedGatewayChannelDoesNotClearExistingValue() {
        VideoGatewayMappingManager manager = mock(VideoGatewayMappingManager.class);
        when(manager.updateById(any(VideoGatewayMapping.class))).thenReturn(true);
        TestGatewayMappingService service = new TestGatewayMappingService(manager);

        service.updateById(VideoGatewayMappingUpdateVO.builder().id(10L).build());

        verify(manager, never()).update(any(Wrapper.class));
    }

    private static final class TestGroupRelationService extends VideoDeviceGroupRelationServiceImpl {
        private VideoDeviceGroupRelation mapSave(VideoDeviceGroupRelationSaveVO saveVO) {
            return saveBefore(saveVO);
        }
    }

    private static final class TestGatewayMappingService extends VideoGatewayMappingServiceImpl {
        private TestGatewayMappingService() {
        }

        private TestGatewayMappingService(VideoGatewayMappingManager manager) {
            this.superManager = manager;
        }

        private VideoGatewayMapping mapSave(VideoGatewayMappingSaveVO saveVO) {
            return saveBefore(saveVO);
        }

        private VideoGatewayMapping mapUpdate(VideoGatewayMappingUpdateVO updateVO) {
            return updateBefore(updateVO);
        }
    }
}
