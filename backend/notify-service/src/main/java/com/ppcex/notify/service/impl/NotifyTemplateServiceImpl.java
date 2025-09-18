package com.ppcex.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.notify.entity.NotifyTemplate;
import com.ppcex.notify.mapper.NotifyTemplateMapper;
import com.ppcex.notify.service.NotifyTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 通知模板服务实现
 */
@Slf4j
@Service
public class NotifyTemplateServiceImpl extends ServiceImpl<NotifyTemplateMapper, NotifyTemplate> implements NotifyTemplateService {

    @Override
    public NotifyTemplate getTemplate(String templateCode, String language) {
        if (!StringUtils.hasText(templateCode)) {
            return null;
        }

        LambdaQueryWrapper<NotifyTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyTemplate::getTemplateCode, templateCode);

        if (StringUtils.hasText(language)) {
            queryWrapper.eq(NotifyTemplate::getLanguage, language);
        }

        return getOne(queryWrapper);
    }

    @Override
    public NotifyTemplate getEnabledTemplate(String templateCode, String language) {
        if (!StringUtils.hasText(templateCode)) {
            return null;
        }

        LambdaQueryWrapper<NotifyTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyTemplate::getTemplateCode, templateCode)
                   .eq(NotifyTemplate::getStatus, 1);

        if (StringUtils.hasText(language)) {
            queryWrapper.eq(NotifyTemplate::getLanguage, language);
        }

        return getOne(queryWrapper);
    }

    @Override
    public NotifyTemplate saveTemplate(NotifyTemplate template) {
        if (template.getId() == null) {
            // 新增模板
            save(template);
        } else {
            // 更新模板
            updateById(template);
        }
        return template;
    }

    @Override
    public boolean deleteTemplate(Long id) {
        return removeById(id);
    }
}