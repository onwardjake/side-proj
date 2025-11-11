package com.jake.landtrade.controller;

import com.jake.landtrade.service.AptTradeIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/ingest")
@RequiredArgsConstructor
public class AdminIngestController {
    private final AptTradeIngestService aptTradeIngestService;

    @PostMapping("/init")
    public String initLoad(
            @RequestParam String fromYm,
            @RequestParam String toYm,
            @RequestParam(defaultValue="150") long delay
    ){
        aptTradeIngestService.initialLoad(fromYm, toYm, delay);
        return "아파트 실거래가 초기 데이터 적재 완료!";
    }

    @PostMapping("/daily")
    public String dailyIncrement(
            @RequestParam(defaultValue = "150") long delay
    ){
        aptTradeIngestService.dailyIncrement(delay);
        return "아파트 실거래가 증분 데이터 적재 완료!";
    }
}
