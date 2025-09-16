package com.ppcex.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserKyc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserKycRepository extends BaseMapper<UserKyc> {

    /**
     * 根据用户ID查询KYC信息
     */
    UserKyc selectByUserId(@Param("userId") Long userId);

    /**
     * 更新KYC审核状态
     */
    int updateAuditStatus(@Param("userId") Long userId,
                          @Param("status") Integer status,
                          @Param("auditUser") String auditUser,
                          @Param("rejectReason") String rejectReason,
                          @Param("auditTime") java.time.LocalDateTime auditTime);

    /**
     * 查询待审核的KYC数量
     */
    Long selectPendingAuditCount();
}