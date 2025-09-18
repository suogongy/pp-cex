package com.ppcex.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.market.entity.MarketPair;
import com.ppcex.market.mapper.MarketPairMapper;
import com.ppcex.market.service.MarketPairService;
import com.ppcex.market.dto.MarketPairVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketPairServiceImpl extends ServiceImpl<MarketPairMapper, MarketPair> implements MarketPairService {

    @Autowired
    private MarketPairMapper marketPairMapper;

    @Override
    @Cacheable(value = "marketPairs", key = "#page.current + '-' + #page.size + '-' + #symbol + '-' + #status")
    public IPage<MarketPairVO> getMarketPairPage(Page<MarketPair> page, String symbol, Integer status) {
        return marketPairMapper.getMarketPairPage(page, symbol, status);
    }

    @Override
    @Cacheable(value = "activeMarketPairs", unless = "#result == null || #result.isEmpty()")
    public List<MarketPairVO> getActiveMarketPairs() {
        return marketPairMapper.getActiveMarketPairs();
    }

    @Override
    @Cacheable(value = "marketPairBySymbol", key = "#symbol", unless = "#result == null")
    public MarketPairVO getMarketPairBySymbol(String symbol) {
        return marketPairMapper.getMarketPairBySymbol(symbol);
    }

    @Override
    @Cacheable(value = "marketPairById", key = "#id", unless = "#result == null")
    public MarketPairVO getMarketPairById(Long id) {
        return marketPairMapper.getMarketPairById(id);
    }

    @Override
    @CacheEvict(value = {"marketPairs", "activeMarketPairs", "marketPairBySymbol", "marketPairById"}, allEntries = true)
    @Transactional
    public boolean addMarketPair(MarketPair marketPair) {
        return save(marketPair);
    }

    @Override
    @CacheEvict(value = {"marketPairs", "activeMarketPairs", "marketPairBySymbol", "marketPairById"}, allEntries = true)
    @Transactional
    public boolean updateMarketPair(MarketPair marketPair) {
        return updateById(marketPair);
    }

    @Override
    @CacheEvict(value = {"marketPairs", "activeMarketPairs", "marketPairBySymbol", "marketPairById"}, allEntries = true)
    @Transactional
    public boolean deleteMarketPair(Long id) {
        return removeById(id);
    }

    @Override
    @CacheEvict(value = {"marketPairs", "activeMarketPairs", "marketPairBySymbol", "marketPairById"}, allEntries = true)
    @Transactional
    public boolean syncFromTradeService() {
        // TODO: 从trade-service同步交易对数据
        // 这里需要调用trade-service的API来获取最新的交易对配置
        log.info("开始同步交易对数据从trade-service");
        return true;
    }

    @Override
    @Cacheable(value = "allMarketPairs", unless = "#result == null || #result.isEmpty()")
    public List<MarketPairVO> getAllMarketPairs() {
        QueryWrapper<MarketPair> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort_order").orderByDesc("create_time");
        List<MarketPair> marketPairs = list(queryWrapper);

        return marketPairs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private MarketPairVO convertToVO(MarketPair marketPair) {
        MarketPairVO vo = new MarketPairVO();
        BeanUtils.copyProperties(marketPair, vo);

        // 设置状态名称
        if (marketPair.getStatus() != null) {
            switch (marketPair.getStatus()) {
                case 1:
                    vo.setStatusName("正常");
                    break;
                case 2:
                    vo.setStatusName("暂停");
                    break;
                default:
                    vo.setStatusName("未知");
            }
        }

        return vo;
    }
}