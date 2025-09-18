package com.ppcex.notify.service;

import com.ppcex.notify.dto.NotifyRequestDTO;
import com.ppcex.notify.dto.NotifyResponseDTO;

/**
 * 通知服务接口
 */
public interface NotifyService {

    /**
     * 发送通知
     *
     * @param requestDTO 通知请求
     * @return 通知响应
     */
    NotifyResponseDTO sendNotify(NotifyRequestDTO requestDTO);

    /**
     * 批量发送通知
     *
     * @param requestDTOList 通知请求列表
     * @return 通知响应列表
     */
    java.util.List<NotifyResponseDTO> batchSendNotify(java.util.List<NotifyRequestDTO> requestDTOList);

    /**
     * 重试发送失败的通知
     *
     * @param notifyNo 通知编号
     * @return 是否重试成功
     */
    boolean retryNotify(String notifyNo);

    /**
     * 获取通知状态
     *
     * @param notifyNo 通知编号
     * @return 通知状态
     */
    Integer getNotifyStatus(String notifyNo);
}