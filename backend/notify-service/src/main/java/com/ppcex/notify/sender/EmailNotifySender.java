package com.ppcex.notify.sender;

import com.ppcex.notify.entity.NotifyRecord;
import com.ppcex.notify.enums.NotifyTypeEnum;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 邮件通知发送器
 */
@Slf4j
@Component
public class EmailNotifySender implements NotifySender {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public boolean send(NotifyRecord record) {
        try {
            log.info("开始发送邮件通知: {}", record.getNotifyNo());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 设置邮件内容
            helper.setFrom("noreply@ppcex.com");
            helper.setTo(record.getRecipient());
            helper.setSubject(record.getTitle());
            helper.setText(record.getContent(), true); // true表示HTML格式

            // 发送邮件
            mailSender.send(message);

            log.info("邮件通知发送成功: {}", record.getNotifyNo());
            return true;

        } catch (Exception e) {
            log.error("邮件通知发送失败: {}", record.getNotifyNo(), e);
            return false;
        }
    }

    @Override
    public Integer getType() {
        return NotifyTypeEnum.EMAIL.getCode();
    }
}