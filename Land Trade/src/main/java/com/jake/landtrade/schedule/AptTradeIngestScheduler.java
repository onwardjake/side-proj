package com.jake.landtrade.schedule;

import com.jake.landtrade.service.AptTradeIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AptTradeIngestScheduler {
    private final AptTradeIngestService aptTradeIngestService;

    // cron scheduler 초 분 시 일 월 요일
    @Scheduled (cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        log.info("[Scheduler] 아파트 실거래가 증분 데이터 수집 시작");
        aptTradeIngestService.dailyIncrement(200);
        log.info("[Scheduler] 아파트 실거래가 증분 데이터 수집 종료");
    }
}
