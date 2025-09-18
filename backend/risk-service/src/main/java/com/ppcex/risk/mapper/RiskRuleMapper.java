package com.ppcex.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppcex.risk.entity.RiskRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 风控规则Mapper接口
 *
 * @author PPCEX Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface RiskRuleMapper extends BaseMapper<RiskRule> {

    /**
     * 分页查询风控规则
     *
     * @param page 分页参数
     * @param ruleType 规则类型
     * @param riskLevel 风险等级
     * @param status 状态
     * @return 分页结果
     */
    IPage<RiskRule> selectRulePage(Page<RiskRule> page,
                                  @Param("ruleType") Integer ruleType,
                                  @Param("riskLevel") Integer riskLevel,
                                  @Param("status") Integer status);

    /**
     * 根据规则类型查询启用的规则
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<RiskRule> selectEnabledRulesByType(@Param("ruleType") Integer ruleType);

    /**
     * 根据规则编码查询规则
     *
     * @param ruleCode 规则编码
     * @return 规则信息
     */
    RiskRule selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 批量更新规则状态
     *
     * @param ids 规则ID列表
     * @param status 状态
     * @return 更新数量
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据规则分类查询规则数量
     *
     * @param ruleCategory 规则分类
     * @return 规则数量
     */
    int countByRuleCategory(@Param("ruleCategory") String ruleCategory);
}