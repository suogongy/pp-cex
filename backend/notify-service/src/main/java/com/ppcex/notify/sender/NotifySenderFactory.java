package com.ppcex.notify.sender;

import com.ppcex.notify.enums.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知发送器工厂
 */
@Slf4j
@Component
public class NotifySenderFactory {

    private final Map<Integer, NotifySender> senderMap;

    @Autowired
    public NotifySenderFactory(List<NotifySender> senders) {
        this.senderMap = senders.stream()
            .collect(Collectors.toMap(NotifySender::getType, Function.identity()));
    }

    /**
     * 获取发送器
     *
     * @param notifyType 通知类型
     * @return 发送器
     */
    public NotifySender getSender(Integer notifyType) {
        NotifySender sender = senderMap.get(notifyType);
        if (sender == null) {
            throw new IllegalArgumentException("不支持的通知类型: " + notifyType);
        }
        return sender;
    }
}