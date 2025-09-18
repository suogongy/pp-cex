package com.ppcex.notify.service;

import com.ppcex.notify.entity.NotifyConfig;

import java.util.List;

/**
 * 通知配置服务接口
 */
public interface NotifyConfigService {

    /**
     * 获取通知配置
     *
     * @param configKey 配置键
     * @return 通知配置
     */
    NotifyConfig getConfig(String configKey);

    /**
     * 获取启用状态的配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    List<NotifyConfig> getEnabledConfigs(Integer configType);

    /**
     * 保存配置
     *
     * @param config 配置信息
     * @return 保存后的配置
     */
    NotifyConfig saveConfig(NotifyConfig config);

    /**
     * 删除配置
     *
     * @param id 配置ID
     * @return 是否删除成功
     */
    boolean deleteConfig(Long id);
}