package com.jake.landtrade.service;

import com.jake.landtrade.client.AptTradeOpenApiClient;
import com.jake.landtrade.dto.OpenApiTradeItem;
import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.mapper.AptTradeMapper;
import com.jake.landtrade.util.AptTradeMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AptTradeService {
    private final AptTradeOpenApiClient client;
    private final AptTradeMapper aptTradeMapper;

    // 페이지 단위로 데이터를 받아오는 메소드
    // 여러 페이지로 구성되어 있을 경우, 특정 페이지만 Open API를 호출해서 가져올 수도 있고,
    // 전체를 가져와서(fetchAll 메소드 사용), 로컬에서 페이지 단위로 이동하는 것을 구현할 수도 있다.
    public List<TradeItem> fetchPage(String lawdCd, String dealYmd, int page, int size) {
        // OpenAPI를 호출해서 xml로 받은 데이터를 OpenApiTradeItem으로 받는다(String 데이터)
        List<OpenApiTradeItem> apiItems = client.fetchPage(lawdCd, dealYmd, page, size).body().items().item();

        // String 형태의 데이터를 DB 필드의 데이터 양식에 맞게 변환한다.
        List<TradeItem> tradeItems = apiItems.stream()
                .map(AptTradeMapperUtil::map)
                .filter(Objects::nonNull)
                .toList();

        // DB에 insert한다.
        aptTradeMapper.bulkUpsert(tradeItems);

        return tradeItems;
    }

    // 법정동코드 및 실거래월에 해당하는 전체 데이터를 받아오는 메소드
    public List<TradeItem> fetchAll(String lawdCd, String dealYmd) {
        // OpenAPI를 호출해서 xml로 받은 데이터를 OpenApiTradeItem으로 받는다(String 데이터)
        List<OpenApiTradeItem> apiItems = client.fetchAll(lawdCd, dealYmd);

        // String 형태의 데이터를 DB 필드의 데이터 양식에 맞게 변환한다.
        List<TradeItem> tradeItems = apiItems.stream()
                .map(AptTradeMapperUtil::map)
                .filter(Objects::nonNull)
                .toList();

        // DB에 insert한다.
        aptTradeMapper.bulkUpsert(tradeItems);

        return tradeItems;
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
