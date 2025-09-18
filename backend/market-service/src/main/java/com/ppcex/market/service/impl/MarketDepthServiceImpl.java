package com.ppcex.market.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppcex.market.entity.MarketDepth;
import com.ppcex.market.mapper.MarketDepthMapper;
import com.ppcex.market.service.MarketDepthService;
import com.ppcex.market.dto.MarketDepthVO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketDepthServiceImpl extends ServiceImpl<MarketDepthMapper, MarketDepth> implements MarketDepthService {

    @Autowired
    private MarketDepthMapper marketDepthMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DEPTH_CACHE_PREFIX = "depth:";
    private static final String DEPTH_SNAPSHOT_PREFIX = "depth:snapshot:";
    private static final int DEFAULT_DEPTH_LIMIT = 20;

    @Override
    @Cacheable(value = "marketDepth", key = "#symbol + ':' + #limit")
    public MarketDepthVO getMarketDepth(String symbol, Integer limit) {
        String cacheKey = DEPTH_CACHE_PREFIX + symbol;

        // 尝试从缓存获取深度数据
        @SuppressWarnings("unchecked")
        MarketDepthVO cachedDepth = (MarketDepthVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedDepth != null) {
            return limitDepth(cachedDepth, limit);
        }

        // 如果缓存中没有，返回空的深度数据
        MarketDepthVO emptyDepth = new MarketDepthVO();
        emptyDepth.setSymbol(symbol);
        emptyDepth.setTimestamp(System.currentTimeMillis());
        emptyDepth.setBids(new ArrayList<>());
        emptyDepth.setAsks(new ArrayList<>());
        emptyDepth.setLastUpdateId(0L);

        return emptyDepth;
    }

    @Override
    @CacheEvict(value = "marketDepth", key = "#symbol")
    @Transactional
    public void updateMarketDepth(String symbol, List<BigDecimal[]> bids, List<BigDecimal[]> asks, Long timestamp) {
        MarketDepthVO depthVO = new MarketDepthVO();
        depthVO.setSymbol(symbol);
        depthVO.setBids(limitDepthList(bids, DEFAULT_DEPTH_LIMIT));
        depthVO.setAsks(limitDepthList(asks, DEFAULT_DEPTH_LIMIT));
        depthVO.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());
        depthVO.setLastUpdateId(System.currentTimeMillis());

        // 更新缓存
        String cacheKey = DEPTH_CACHE_PREFIX + symbol;
        redisTemplate.opsForValue().set(cacheKey, depthVO, 30, TimeUnit.SECONDS);

        log.debug("更新深度数据: {} bids={} asks={}", symbol, bids.size(), asks.size());
    }

    @Override
    @Transactional
    public void saveMarketDepthSnapshot(String symbol, List<BigDecimal[]> bids, List<BigDecimal[]> asks, Long timestamp) {
        MarketDepth depth = new MarketDepth();
        depth.setSymbol(symbol);
        depth.setBids(JSON.toJSONString(limitDepthList(bids, DEFAULT_DEPTH_LIMIT)));
        depth.setAsks(JSON.toJSONString(limitDepthList(asks, DEFAULT_DEPTH_LIMIT)));
        depth.setTimestamp(timestamp != null ? timestamp : System.currentTimeMillis());

        save(depth);

        // 同时保存到Redis用于快速访问
        String snapshotKey = DEPTH_SNAPSHOT_PREFIX + symbol + ":" + timestamp;
        redisTemplate.opsForValue().set(snapshotKey, depth, 1, TimeUnit.HOURS);

        log.debug("保存深度快照: {} timestamp={}", symbol, timestamp);
    }

    @Override
    @Cacheable(value = "depthHistory", key = "#symbol + ':' + #limit")
    public List<MarketDepthVO> getDepthHistory(String symbol, Integer limit) {
        List<MarketDepth> depths = list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MarketDepth>()
                .eq("symbol", symbol)
                .orderByDesc("timestamp")
                .last("LIMIT " + (limit != null ? limit : 100)));

        return depths.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"marketDepth", "depthHistory"}, allEntries = true)
    @Transactional
    public void clearDepthCache() {
        // 清除Redis中的所有深度缓存
        redisTemplate.delete(redisTemplate.keys(DEPTH_CACHE_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(DEPTH_SNAPSHOT_PREFIX + "*"));

        log.info("清除所有深度数据缓存");
    }

    private MarketDepthVO convertToVO(MarketDepth depth) {
        MarketDepthVO vo = new MarketDepthVO();
        vo.setSymbol(depth.getSymbol());
        vo.setTimestamp(depth.getTimestamp());

        try {
            // 解析JSON字符串
            JSONArray bidsArray = JSON.parseArray(depth.getBids());
            JSONArray asksArray = JSON.parseArray(depth.getAsks());

            List<BigDecimal[]> bids = new ArrayList<>();
            List<BigDecimal[]> asks = new ArrayList<>();

            for (int i = 0; i < bidsArray.size(); i++) {
                JSONArray item = bidsArray.getJSONArray(i);
                bids.add(new BigDecimal[]{item.getBigDecimal(0), item.getBigDecimal(1)});
            }

            for (int i = 0; i < asksArray.size(); i++) {
                JSONArray item = asksArray.getJSONArray(i);
                asks.add(new BigDecimal[]{item.getBigDecimal(0), item.getBigDecimal(1)});
            }

            vo.setBids(bids);
            vo.setAsks(asks);
        } catch (Exception e) {
            log.error("解析深度数据失败: {}", depth.getSymbol(), e);
            vo.setBids(new ArrayList<>());
            vo.setAsks(new ArrayList<>());
        }

        return vo;
    }

    private List<BigDecimal[]> limitDepthList(List<BigDecimal[]> depthList, Integer limit) {
        if (depthList == null || depthList.isEmpty()) {
            return new ArrayList<>();
        }

        int actualLimit = limit != null ? limit : DEFAULT_DEPTH_LIMIT;
        return depthList.stream()
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    private MarketDepthVO limitDepth(MarketDepthVO depth, Integer limit) {
        if (depth == null) {
            return null;
        }

        MarketDepthVO limitedDepth = new MarketDepthVO();
        limitedDepth.setSymbol(depth.getSymbol());
        limitedDepth.setTimestamp(depth.getTimestamp());
        limitedDepth.setBids(limitDepthList(depth.getBids(), limit));
        limitedDepth.setAsks(limitDepthList(depth.getAsks(), limit));
        limitedDepth.setLastUpdateId(depth.getLastUpdateId());

        return limitedDepth;
    }

    /**
     * 整理深度数据 - 合并相同价格的订单
     */
    private List<BigDecimal[]> consolidateDepth(List<BigDecimal[]> depthList) {
        if (depthList == null || depthList.isEmpty()) {
            return new ArrayList<>();
        }

        return depthList.stream()
                .collect(Collectors.groupingBy(arr -> arr[0]))
                .entrySet().stream()
                .map(entry -> new BigDecimal[]{
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(arr -> arr[1])
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                })
                .sorted((arr1, arr2) -> {
                    // 买单按价格降序排列，卖单按价格升序排列
                    return arr2[0].compareTo(arr1[0]);
                })
                .collect(Collectors.toList());
    }
}