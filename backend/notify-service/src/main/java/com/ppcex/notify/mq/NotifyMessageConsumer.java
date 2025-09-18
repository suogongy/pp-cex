package com.ppcex.notify.mq;

import com.alibaba.fastjson2.JSON;
import com.ppcex.notify.dto.NotifyRequestDTO;
import com.ppcex.notify.service.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Bean;
import java.util.function.Consumer;

/**
 * 通知消息消费者
 */
@Slf4j
@Service
public class NotifyMessageConsumer {

    @Autowired
    private NotifyService notifyService;

    @Bean
    public Consumer<Message<String>> notifyTopic() {
        return this::handleNotifyMessage;
    }

    private void handleNotifyMessage(Message<String> message) {
        try {
            String payload = message.getPayload();
            String tags = (String) message.getHeaders().get("tags");

            log.info("收到通知消息: tags={}, payload={}", tags, payload);

            if ("NOTIFY_SEND".equals(tags)) {
                // 解析消息
                NotifyRequestDTO requestDTO = JSON.parseObject(payload, NotifyRequestDTO.class);

                // 发送通知
                notifyService.sendNotify(requestDTO);

                log.info("通知消息处理完成: {}", requestDTO);
            } else {
                log.warn("未知的消息标签: {}", tags);
            }

        } catch (Exception e) {
            log.error("通知消息处理失败", e);
            throw new RuntimeException("通知消息处理失败", e);
        }
    }
}