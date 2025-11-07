package com.jake.landtrade.controller;

import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.service.LawdCdService;
import com.jake.landtrade.service.AptTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/apt-trades")
@RequiredArgsConstructor
public class AptTradeController {
    private final AptTradeService aptTradeService;
    private final LawdCdService lawdCdService;

    @GetMapping("/test")
    public String test() {
        lawdCdService.getAllLawdCds();
        return "success";
    }

    // http://localhost:8080/api/trades?lawdCd=41135&dealYmd=202510&page=1&size=10
    @GetMapping
    public List<TradeItem> getAptTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd,
            @RequestParam(defaultValue="1") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return aptTradeService.fetchPage(lawdCd, dealYmd, page, size);
    }

    // http://localhost:8080/api/trades/all?lawdCd=41135&dealYmd=202510
    @GetMapping("/all")
    public List<TradeItem> getAllAptTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd
    ){
        return aptTradeService.fetchAll(lawdCd, dealYmd);
    }
}
