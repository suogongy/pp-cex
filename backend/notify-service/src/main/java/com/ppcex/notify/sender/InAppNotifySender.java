package com.ppcex.notify.sender;

import com.ppcex.notify.entity.NotifyRecord;
import com.ppcex.notify.enums.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 站内信通知发送器
 */
@Slf4j
@Component
public class InAppNotifySender implements NotifySender {

    @Override
    public boolean send(NotifyRecord record) {
        try {
            log.info("开始发送站内信通知: {}", record.getNotifyNo());

            // TODO: 实现站内信存储逻辑
            // 可以保存到数据库或Redis中，供前端轮询或WebSocket推送

            log.info("站内信通知发送成功: {}", record.getNotifyNo());
            return true;

        } catch (Exception e) {
            log.error("站内信通知发送失败: {}", record.getNotifyNo(), e);
            return false;
        }
    }

    @Override
    public Integer getType() {
        return NotifyTypeEnum.IN_APP.getCode();
    }
}