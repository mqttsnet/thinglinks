package com.mqttsnet.thinglinks.dto.bridge.protocol;

/**
 * 协议级凭证 DTO 标记接口。
 *
 * <p>与 {@link ProtocolConnectionDto} 区别：本接口对应字段在 DB 走 {@code EncryptTypeHandler}
 * 整体加密落盘，<b>列表接口屏蔽明文</b>，仅 detail 接口 + Connector 实例化时短暂解密。
 *
 * @author mqttsnet
 */
public interface ProtocolCredentialDto {
}
