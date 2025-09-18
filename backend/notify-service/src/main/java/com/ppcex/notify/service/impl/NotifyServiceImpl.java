package com.ppcex.notify.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.notify.dto.NotifyRequestDTO;
import com.ppcex.notify.dto.NotifyResponseDTO;
import com.ppcex.notify.entity.NotifyRecord;
import com.ppcex.notify.entity.NotifyTemplate;
import com.ppcex.notify.entity.UserNotifyPreference;
import com.ppcex.notify.enums.NotifyStatusEnum;
import com.ppcex.notify.enums.NotifyTypeEnum;
import com.ppcex.notify.mapper.NotifyRecordMapper;
import com.ppcex.notify.mapper.UserNotifyPreferenceMapper;
import com.ppcex.notify.service.NotifyService;
import com.ppcex.notify.service.NotifyTemplateService;
import com.ppcex.notify.sender.NotifySender;
import com.ppcex.notify.sender.NotifySenderFactory;
import com.ppcex.notify.util.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 通知服务实现
 */
@Slf4j
@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyRecordMapper, NotifyRecord> implements NotifyService {

    @Autowired
    private NotifyTemplateService notifyTemplateService;

    @Autowired
    private NotifySenderFactory notifySenderFactory;

    @Autowired
    private UserNotifyPreferenceMapper userNotifyPreferenceMapper;

    @Override
    public NotifyResponseDTO sendNotify(NotifyRequestDTO requestDTO) {
        try {
            // 验证用户通知偏好
            if (!checkUserNotifyPreference(requestDTO)) {
                return createErrorResponse("用户已禁用此类通知");
            }

            // 获取模板
            NotifyTemplate template = notifyTemplateService.getEnabledTemplate(
                requestDTO.getTemplateCode(), requestDTO.getLanguage());

            if (template == null) {
                return createErrorResponse("模板不存在或已禁用");
            }

            // 处理模板变量
            String title = TemplateUtil.processTemplate(template.getTemplateName(), requestDTO.getTemplateVars());
            String content = TemplateUtil.processTemplate(template.getTemplateContent(), requestDTO.getTemplateVars());

            // 创建通知记录
            NotifyRecord record = createNotifyRecord(requestDTO, title, content);

            // 异步发送通知
            asyncSendNotify(record);

            // 返回响应
            NotifyResponseDTO response = new NotifyResponseDTO();
            response.setNotifyNo(record.getNotifyNo());
            response.setStatus(record.getStatus());
            response.setStatusDesc(NotifyStatusEnum.SENDING.getDescription());
            response.setCreateTime(record.getCreateTime());
            response.setMessage("通知已提交发送");

            return response;

        } catch (Exception e) {
            log.error("发送通知失败", e);
            return createErrorResponse("发送通知失败: " + e.getMessage());
        }
    }

    @Override
    public List<NotifyResponseDTO> batchSendNotify(List<NotifyRequestDTO> requestDTOList) {
        List<NotifyResponseDTO> responseList = new ArrayList<>();

        for (NotifyRequestDTO requestDTO : requestDTOList) {
            responseList.add(sendNotify(requestDTO));
        }

        return responseList;
    }

    @Override
    public boolean retryNotify(String notifyNo) {
        try {
            NotifyRecord record = getNotifyRecord(notifyNo);
            if (record == null) {
                log.warn("通知记录不存在: {}", notifyNo);
                return false;
            }

            if (record.getStatus() == NotifyStatusEnum.SENT.getCode()) {
                log.info("通知已发送，无需重试: {}", notifyNo);
                return true;
            }

            if (record.getSendCount() >= record.getMaxRetry()) {
                log.warn("通知已达到最大重试次数: {}", notifyNo);
                return false;
            }

            // 更新重试信息
            record.setSendCount(record.getSendCount() + 1);
            record.setNextRetryTime(LocalDateTime.now().plusMinutes(5 * record.getSendCount()));
            updateById(record);

            // 异步发送
            asyncSendNotify(record);

            return true;

        } catch (Exception e) {
            log.error("重试通知失败: {}", notifyNo, e);
            return false;
        }
    }

    @Override
    public Integer getNotifyStatus(String notifyNo) {
        NotifyRecord record = getNotifyRecord(notifyNo);
        return record != null ? record.getStatus() : null;
    }

    /**
     * 验证用户通知偏好
     */
    private boolean checkUserNotifyPreference(NotifyRequestDTO requestDTO) {
        if (requestDTO.getUserId() == null) {
            return true; // 非用户通知直接放行
        }

        LambdaQueryWrapper<UserNotifyPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserNotifyPreference::getUserId, requestDTO.getUserId())
                   .eq(UserNotifyPreference::getNotifyType, requestDTO.getNotifyType())
                   .eq(UserNotifyPreference::getBusinessType, requestDTO.getBusinessType())
                   .eq(UserNotifyPreference::getEnabled, 1);

        UserNotifyPreference preference = userNotifyPreferenceMapper.selectOne(queryWrapper);
        return preference != null;
    }

    /**
     * 创建通知记录
     */
    private NotifyRecord createNotifyRecord(NotifyRequestDTO requestDTO, String title, String content) {
        NotifyRecord record = new NotifyRecord();
        record.setNotifyNo(generateNotifyNo());
        record.setBusinessType(requestDTO.getBusinessType());
        record.setNotifyType(requestDTO.getNotifyType());
        record.setUserId(requestDTO.getUserId());
        record.setRecipient(requestDTO.getRecipient());
        record.setTitle(title);
        record.setContent(content);
        record.setTemplateCode(requestDTO.getTemplateCode());
        record.setTemplateVars(JSON.toJSONString(requestDTO.getTemplateVars()));
        record.setStatus(NotifyStatusEnum.PENDING.getCode());
        record.setSendCount(0);
        record.setMaxRetry(3);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        save(record);
        return record;
    }

    /**
     * 异步发送通知
     */
    private void asyncSendNotify(NotifyRecord record) {
        // 更新状态为发送中
        record.setStatus(NotifyStatusEnum.SENDING.getCode());
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);

        // 使用线程池异步发送
        new Thread(() -> {
            try {
                NotifySender sender = notifySenderFactory.getSender(record.getNotifyType());
                boolean success = sender.send(record);

                // 更新发送结果
                if (success) {
                    record.setStatus(NotifyStatusEnum.SENT.getCode());
                    record.setSendTime(LocalDateTime.now());
                } else {
                    record.setStatus(NotifyStatusEnum.FAILED.getCode());
                    record.setErrorMsg("发送失败");
                }

                record.setUpdateTime(LocalDateTime.now());
                updateById(record);

            } catch (Exception e) {
                log.error("异步发送通知失败: {}", record.getNotifyNo(), e);
                record.setStatus(NotifyStatusEnum.FAILED.getCode());
                record.setErrorMsg(e.getMessage());
                record.setUpdateTime(LocalDateTime.now());
                updateById(record);
            }
        }).start();
    }

    /**
     * 获取通知记录
     */
    private NotifyRecord getNotifyRecord(String notifyNo) {
        LambdaQueryWrapper<NotifyRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyRecord::getNotifyNo, notifyNo);
        return getOne(queryWrapper);
    }

    /**
     * 生成通知编号
     */
    private String generateNotifyNo() {
        return "NTF" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 创建错误响应
     */
    private NotifyResponseDTO createErrorResponse(String message) {
        NotifyResponseDTO response = new NotifyResponseDTO();
        response.setStatus(NotifyStatusEnum.FAILED.getCode());
        response.setStatusDesc(NotifyStatusEnum.FAILED.getDescription());
        response.setMessage(message);
        return response;
    }
}