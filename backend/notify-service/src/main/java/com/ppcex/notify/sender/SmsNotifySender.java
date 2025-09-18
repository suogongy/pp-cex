package com.ppcex.notify.sender;

import com.ppcex.notify.entity.NotifyRecord;
import com.ppcex.notify.enums.NotifyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信通知发送器
 */
@Slf4j
@Component
public class SmsNotifySender implements NotifySender {

    @Override
    public boolean send(NotifyRecord record) {
        try {
            log.info("开始发送短信通知: {}", record.getNotifyNo());

            // TODO: 集成第三方短信服务商
            // 这里是示例实现，实际使用时需要集成阿里云、腾讯云等短信服务

            String phone = record.getRecipient();
            String content = record.getContent();

            // 模拟发送短信
            log.info("发送短信到: {}, 内容: {}", phone, content);

            // 模拟发送成功
            log.info("短信通知发送成功: {}", record.getNotifyNo());
            return true;

        } catch (Exception e) {
            log.error("短信通知发送失败: {}", record.getNotifyNo(), e);
            return false;
        }
    }

    @Override
    public Integer getType() {
        return NotifyTypeEnum.SMS.getCode();
    }
}