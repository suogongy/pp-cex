package com.ppcex.notify.sender;

import com.ppcex.notify.entity.NotifyRecord;

/**
 * 通知发送器接口
 */
public interface NotifySender {

    /**
     * 发送通知
     *
     * @param record 通知记录
     * @return 是否发送成功
     */
    boolean send(NotifyRecord record);

    /**
     * 获取发送器类型
     *
     * @return 发送器类型
     */
    Integer getType();
}