package com.jake.landtrade.service;

import com.jake.landtrade.client.AptTradeOpenApiClient;
import com.jake.landtrade.dto.OpenApiTradeItem;
import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.mapper.AptTradeMapper;
import com.jake.landtrade.mapper.LawdCodeMapper;
import com.jake.landtrade.util.AptTradeMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AptTradeIngestService {
    private final AptTradeOpenApiClient aptTradeOpenApiClient;
    private final AptTradeMapper aptTradeMapper;
    private final LawdCodeMapper lawdCodeMapper;

    // 초기 적재
    // 대상 : 전국
    // 기간 : fromYm ~ toYm
    @Transactional
    public void initialLoad(String fromYm, String toYm, long delayMills) {
        List<String> lawds = lawdCodeMapper.selectAllLawdCdSgg();
        log.info("아파트 실거래가 Initial load: {} ~ {}, LAWD size={}", fromYm, toYm, lawds.size());
        List<String> months = ymRange(fromYm, toYm);

        for( String lawd : lawds ) {
            for (String month : months) {
                ingestLawdYm(lawd, month, delayMills);
            }
        }
    }

    // 매일 증분
    @Transactional
    public void dailyIncrement(long delayMills) {
        String ym = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        List<String> lawds = lawdCodeMapper.selectAllLawdCdSgg();
        log.info("아파트 실거래가 증분 데이터 수집: {}", ym);

        for(String lawd : lawds) {
            ingestLawdYm(lawd, ym, delayMills);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : ingestLawdYm
    // description : Open API를 호출해서 실거래 데이터를 받아와서 DB에 저장한다.
    // parameter : lawd - 시군구코드(ex. 41135), Ym - 거래년월(ex. 202510), delayMills - delay 시간 (밀리초)
    // return type : none
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void ingestLawdYm(String lawdCd, String dealYmd, long delayMills) {
        try {
            log.info("Ingest start: lawd={}, ym={}", lawdCd, dealYmd);
            List<OpenApiTradeItem> items = aptTradeOpenApiClient.fetchAll(lawdCd, dealYmd);

            // Open API 호출로 받아온 데이터가 없는 경우
            if(items == null || items.isEmpty()) {
                log.info("No data: lawd={}, ym={}", lawdCd, dealYmd);
                sleep(delayMills);
                return ;
            }

            // String 형태의 데이터를 DB 필드의 데이터 양식에 맞게 변환한다.
            List<TradeItem> tradeItems = items.stream()
                    .map(AptTradeMapperUtil::map)
                    .filter(Objects::nonNull)
                    .toList();

            // 1000 건씩 분할해서 upsert 한다.
            int chunk = 1000;
            for(int i=0; i<tradeItems.size(); i+=chunk) {
                int end = Math.min(i+chunk, tradeItems.size());
                List<TradeItem> sub = tradeItems.subList(i, end);
                int affected = aptTradeMapper.bulkUpsert(sub);
                log.info("Upsert {} rows ({}~{} of {}, lawd={}, ym={}", affected, i+1, end, tradeItems.size(), lawdCd, dealYmd);
            }

            sleep(delayMills);
        } catch (Exception e) {
            log.error("Ingest error: lawd={}, ym={}, err={}", lawdCd, dealYmd, e.getMessage());
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : ymRange
    // description : 시작 YYYYMM, 종료 YYYYMM 데이터를 받아서 시작, 종료 기간 사이의 월을 List로 만든다.
    // parameter : fromYm - 시작 거래년월(ex. 202510), toYm - 종료 거래년월
    // return type : 시작 ~ 종료 기간의 월(ex. 202501 ~ 202510) String list를 리턴한다.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<String> ymRange(String fromYm, String toYm) {
        // YYYYMM 데이터에 DD ("01")을 추가한 다음, YYYYMMDD 형태의 날짜 데이터로 변환한다.
        LocalDate from = LocalDate.parse(fromYm + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate to = LocalDate.parse(toYm + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 종료일이 시작일보다 빠르면 시작 YYYYMM를 리턴한다
        if(from.isBefore(to)) {
            return List.of(fromYm);
        }

        // 시작월부터 종료월까지 월 단위로 만들어서 list에 추가한다
        List<String> list = new ArrayList<>();
        LocalDate cur = from.withDayOfMonth(1); // from 객체의 일자를 1로 바꾼 새로운 객체를 반환한다. (ex. 2025-11-10 -> 2025-11-01)
        while (!cur.isAfter(to)) {
            list.add(cur.format(DateTimeFormatter.ofPattern("yyyyMM")));
            cur = cur.plusMonths(1);
        }

        return list;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : sleep
    // description : Open API 요청 사이에 delay를 주기 위해 해당 시간만큼 sleep 한다
    // parameter : 시간 (밀리초)
    // return type : none
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void sleep(long ms) {
        if (ms > 0) {
            try {
                TimeUnit.MILLISECONDS.sleep(ms);
            } catch (InterruptedException e) {
            }
        }
    }
}
