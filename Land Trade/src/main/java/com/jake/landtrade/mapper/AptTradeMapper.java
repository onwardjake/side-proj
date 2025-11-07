package com.jake.landtrade.mapper;

import com.jake.landtrade.dto.TradeItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AptTradeMapper {
    // TABLE: apt_trade
    int bulkUpsert(@Param("list") List<TradeItem> list);

}
