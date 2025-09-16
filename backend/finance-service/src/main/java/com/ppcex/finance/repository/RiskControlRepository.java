package com.ppcex.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.finance.entity.RiskControl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RiskControlRepository extends BaseMapper<RiskControl> {

    @Select("SELECT * FROM risk_control WHERE enabled = true ORDER BY priority ASC")
    List<RiskControl> getEnabledRules();

    @Select("SELECT * FROM risk_control WHERE rule_type = #{ruleType} AND enabled = true")
    List<RiskControl> getRulesByType(@Param("ruleType") Integer ruleType);

    @Select("SELECT * FROM risk_control WHERE rule_code = #{ruleCode} AND enabled = true")
    RiskControl getRuleByCode(@Param("ruleCode") String ruleCode);

    @Select("SELECT * FROM risk_control WHERE risk_level >= #{riskLevel} AND enabled = true ORDER BY priority ASC")
    List<RiskControl> getHighRiskRules(@Param("riskLevel") Integer riskLevel);
}