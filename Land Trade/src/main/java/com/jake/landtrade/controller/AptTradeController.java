package com.jake.landtrade.controller;

import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.service.AptTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/apt-trades")
@RequiredArgsConstructor
public class AptTradeController {
    private final AptTradeService aptTradeService;

    // 호출 예시 : localhost:8080/apt_trades
    @GetMapping
    public List<TradeItem> getAptTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd,
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return aptTradeService.fetchPage(lawdCd, dealYmd, page, size);
    }

    // 호출 예시 : localhost:8080/apt_trades/all
    @GetMapping("/all")
    public List<TradeItem> getAllAptTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd
    ){
        return aptTradeService.fetchAll(lawdCd, dealYmd);
    }
}
