package com.mqttsnet.thinglinks.broker.Actors;

import io.github.quickmsg.common.annotation.AllowCors;
import io.github.quickmsg.common.annotation.Router;
import io.github.quickmsg.common.config.Configuration;
import io.github.quickmsg.common.enums.HttpType;
import io.github.quickmsg.common.message.HttpPublishMessage;
import io.github.quickmsg.core.http.AbstractHttpActor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Description: Broker推送设备消息
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/25$ 17:46$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/25$ 17:46$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Router(value = "/publish", type = HttpType.POST)
@Slf4j
@AllowCors
public class PublishActor extends AbstractHttpActor {

    @Override
    public Publisher<Void> doRequest(HttpServerRequest request, HttpServerResponse response, Configuration httpConfiguration) {
        Charset charset = Charset.defaultCharset();
        log.info("Charset is {}",charset);
        return request
                .receive()
                .asString(StandardCharsets.UTF_8)
                .map(this.toJson(HttpPublishMessage.class))
                .doOnNext(message -> {
                    this.sendMqttMessage(message.getPublishMessage());
                    log.info("http request url {} body {}", request.path(), message);
                }).then(response.sendString(Mono.just("success")).then());
    }
}
