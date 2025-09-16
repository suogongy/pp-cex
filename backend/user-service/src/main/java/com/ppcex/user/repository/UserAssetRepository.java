package com.ppcex.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.user.entity.UserAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserAssetRepository extends BaseMapper<UserAsset> {

    /**
     * 根据用户ID查询所有资产
     */
    List<UserAsset> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和币种ID查询资产
     */
    UserAsset selectByUserIdAndCoinId(@Param("userId") Long userId, @Param("coinId") String coinId);

    /**
     * 更新可用余额
     */
    int updateAvailableBalance(@Param("userId") Long userId,
                               @Param("coinId") String coinId,
                               @Param("amount") BigDecimal amount);

    /**
     * 更新冻结余额
     */
    int updateFrozenBalance(@Param("userId") Long userId,
                             @Param("coinId") String coinId,
                             @Param("amount") BigDecimal amount);

    /**
     * 冻结资产
     */
    int freezeAsset(@Param("userId") Long userId,
                    @Param("coinId") String coinId,
                    @Param("amount") BigDecimal amount);

    /**
     * 解冻资产
     */
    int unfreezeAsset(@Param("userId") Long userId,
                      @Param("coinId") String coinId,
                      @Param("amount") BigDecimal amount);

    /**
     * 更新充值地址
     */
    int updateDepositAddress(@Param("userId") Long userId,
                             @Param("coinId") String coinId,
                             @Param("address") String address);

    /**
     * 查询用户总资产价值（按USD计算）
     */
    BigDecimal selectTotalAssetValue(@Param("userId") Long userId);
}