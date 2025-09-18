package com.ppcex.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.notify.entity.NotifyConfig;
import com.ppcex.notify.mapper.NotifyConfigMapper;
import com.ppcex.notify.service.NotifyConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 通知配置服务实现
 */
@Slf4j
@Service
public class NotifyConfigServiceImpl extends ServiceImpl<NotifyConfigMapper, NotifyConfig> implements NotifyConfigService {

    @Override
    public NotifyConfig getConfig(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return null;
        }

        LambdaQueryWrapper<NotifyConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyConfig::getConfigKey, configKey)
                   .eq(NotifyConfig::getStatus, 1);

        return getOne(queryWrapper);
    }

    @Override
    public List<NotifyConfig> getEnabledConfigs(Integer configType) {
        LambdaQueryWrapper<NotifyConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotifyConfig::getStatus, 1);

        if (configType != null) {
            queryWrapper.eq(NotifyConfig::getConfigType, configType);
        }

        return list(queryWrapper);
    }

    @Override
    public NotifyConfig saveConfig(NotifyConfig config) {
        if (config.getId() == null) {
            // 新增配置
            save(config);
        } else {
            // 更新配置
            updateById(config);
        }
        return config;
    }

    @Override
    public boolean deleteConfig(Long id) {
        return removeById(id);
    }
}