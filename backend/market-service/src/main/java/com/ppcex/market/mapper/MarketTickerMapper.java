package com.ppcex.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ppcex.market.entity.MarketTicker;
import com.ppcex.market.dto.MarketTickerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketTickerMapper extends BaseMapper<MarketTicker> {

    MarketTickerVO getTickerBySymbol(@Param("symbol") String symbol);

    List<MarketTickerVO> getAllTickers();

    int updateTickerBySymbol(@Param("symbol") String symbol,
                           @Param("lastPrice") java.math.BigDecimal lastPrice,
                           @Param("highPrice") java.math.BigDecimal highPrice,
                           @Param("lowPrice") java.math.BigDecimal lowPrice,
                           @Param("volume") java.math.BigDecimal volume,
                           @Param("quoteVolume") java.math.BigDecimal quoteVolume,
                           @Param("count") Integer count);

    int batchUpdateTickers(@Param("tickers") List<MarketTicker> tickers);
}