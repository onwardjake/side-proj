package com.jake.landtrade.service;

import com.jake.landtrade.client.AptTradeOpenApiClient;
import com.jake.landtrade.dto.TradeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AptTradeService {
    private final AptTradeOpenApiClient client;

    // 페이지 단위로 데이터를 받아오는 메소드
    // 여러 페이지로 구성되어 있을 경우, 특정 페이지만 Open API를 호출해서 가져올 수도 있고,
    // 전체를 가져와서(fetchAll 메소드 사용), 로컬에서 페이지 단위로 이동하는 것을 구현할 수도 있다.
    public List<TradeItem> fetchPage(String lawdCd, String dealYmd, int page, int size) {
        return client.fetchPage(lawdCd, dealYmd, page, size).body().items().item();
    }

    // 법정동코드 및 실거래월에 해당하는 전체 데이터를 받아오는 메소드
    public List<TradeItem> fetchAll(String lawdCd, String dealYmd) {
        return client.fetchAll(lawdCd, dealYmd);
    }

    // 거래금액의 숫자단위 구분자 ,를 없애준다: 120,000 -> 120000
    public static Integer parseDealAmount(String dealAmount) {
        if (dealAmount == null) {
            return null;
        }

        // regular expression을 이용해서 숫자가 아닌 문자를 ""로 치환한다.
        String strDigits = dealAmount.replace("[^0-9]", "");

        return strDigits.isEmpty() ? null : Integer.parseInt(strDigits);
    }

    // 년, 월, 일 각각 존재하는 데이터를 LocalDate 타입의 YYYY-MM-DD 형태로 변환한다.
    public static LocalDate toLocalDate(String dealYear, String dealMonth, String dealDay) {
        String dealDate = "%s-%02d-%02d".formatted(dealYear.trim(), Integer.parseInt(dealMonth.trim()), Integer.parseInt(dealDay.trim()));
        return LocalDate.parse(dealDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
