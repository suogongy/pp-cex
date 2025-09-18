package com.ppcex.notify.service;

import com.ppcex.notify.entity.NotifyTemplate;

/**
 * 通知模板服务接口
 */
public interface NotifyTemplateService {

    /**
     * 获取模板
     *
     * @param templateCode 模板编码
     * @param language     语言
     * @return 通知模板
     */
    NotifyTemplate getTemplate(String templateCode, String language);

    /**
     * 获取启用状态的模板
     *
     * @param templateCode 模板编码
     * @param language     语言
     * @return 通知模板
     */
    NotifyTemplate getEnabledTemplate(String templateCode, String language);

    /**
     * 保存模板
     *
     * @param template 模板信息
     * @return 保存后的模板
     */
    NotifyTemplate saveTemplate(NotifyTemplate template);

    /**
     * 删除模板
     *
     * @param id 模板ID
     * @return 是否删除成功
     */
    boolean deleteTemplate(Long id);
}