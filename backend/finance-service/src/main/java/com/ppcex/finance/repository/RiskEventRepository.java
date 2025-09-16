package com.ppcex.finance.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.finance.entity.RiskEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RiskEventRepository extends BaseMapper<RiskEvent> {

    @Select("SELECT * FROM risk_event WHERE user_id = #{userId} ORDER BY create_time DESC LIMIT #{limit}")
    List<RiskEvent> getUserRecentEvents(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT * FROM risk_event WHERE status = #{status} ORDER BY create_time ASC")
    List<RiskEvent> getEventsByStatus(@Param("status") Integer status);

    @Select("SELECT * FROM risk_event WHERE risk_level >= #{riskLevel} AND status = 1 ORDER BY create_time ASC")
    List<RiskEvent> getHighRiskEvents(@Param("riskLevel") Integer riskLevel);

    @Select("SELECT COALESCE(COUNT(*), 0) FROM risk_event WHERE user_id = #{userId} AND create_time >= #{since}")
    Long getUserEventCountSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Select("SELECT * FROM risk_event WHERE event_type = #{eventType} AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<RiskEvent> getEventsByTypeAndTimeRange(@Param("eventType") Integer eventType, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}