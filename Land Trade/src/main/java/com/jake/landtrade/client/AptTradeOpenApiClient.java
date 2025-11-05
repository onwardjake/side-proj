package com.jake.landtrade.client;

import com.jake.landtrade.dto.ApiResponse;
import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.dto.Body;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class AptTradeOpenApiClient {
    private final WebClient webClient;
    private final String servicePath;
    private final String serviceKey;
    private final int defaultPageSize;

    public AptTradeOpenApiClient(
            WebClient.Builder webClientBuilder,
            @Value("${com.jake.landtrade.apt.base-url}") String baseUrl,
            @Value("${com.jake.landtrade.apt.service-path}") String servicePath,
            @Value("${com.jake.landtrade.apt.service-key}")  String serviceKey,
            @Value("${com.jake.landtrade.apt.default-page-size}") int defaultPageSize
    ){
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.servicePath = servicePath;
        this.serviceKey = serviceKey;
        this.defaultPageSize = defaultPageSize;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : fetchPage
    // description : 페이지 단위로 Open API를 호출하여 결과를 받아온다.
    // parameter : lawdCd - 법정동코드 5자리(ex. 11110), dealYmd - 거래년월(ex. 202510)
    //             pageNo - 요청 페이지 번호, pageSize - 페이지당 항목 개수
    // return type : 받아온 실거래가 데이터를 TradeItem 리스트로 만들어 리턴한다
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ApiResponse fetchPage(String lawdCd, String dealYmd, Integer pageNo, Integer pageSize){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("serviceKey", serviceKey);
        params.add("LAWD_CD", lawdCd);
        params.add("DEAL_YMN", dealYmd);
        params.add("pageNo", String.valueOf(Optional.ofNullable(pageNo).orElse(1)));
        params.add("numOfRows", String.valueOf(Optional.ofNullable(pageSize).orElse(defaultPageSize)));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(servicePath)
                        .queryParams(params)
                        .build())
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException("OpenAPI 호출 실패: " + ex.getMessage(), ex)))
                .block();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : fetchAll
    // description : 페이지 개수 만큼 fetchPage를 호출하여 전체 데이터를 받는다.
    // parameter : lawdCd - 법정동코드 5자리(ex. 11110), dealYmd - 거래년월(ex. 202510)
    // return type : 받아온 실거래가 데이터를 TradeItem 리스트로 만들어 리턴한다
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public List<TradeItem> fetchAll(String lawdCd, String dealYmd) {
        int page = 1;
        int rows = defaultPageSize;

        ApiResponse first = fetchPage(lawdCd, dealYmd, page, rows);
        Body body = ensureSuccess(first);

        int total = Optional.ofNullable(body.totalCount()).orElse(0);
        List<TradeItem> all = new ArrayList<>();
        if(body.items() != null && body.items().item() != null) {
            all.addAll(body.items().item());
        }

        int pageCount = (int) Math.ceil(total/(double)rows);
        for(page = 2; page <= pageCount; page++) {
            ApiResponse resp = fetchPage(lawdCd, dealYmd, page, rows);
            Body b = ensureSuccess(resp);
            if(b.items() != null && b.items().item() != null) {
                all.addAll(b.items().item());
            }
        }

        return all;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : ensureSuccess
    // description : API 호출에 대한 응답에 문제가 없는지 확인하고, 문제가 없다면 repose의 body를 리턴한다.
    // parameter : API 호출에 대한 결과가 들어있는 response 객체
    // return type : response 객체의 body를 리턴한다.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static Body ensureSuccess(ApiResponse response) {
        if(response == null || response.header() == null) {
            throw new IllegalStateException("응답 파싱 실패 : 응답 내용이 없거나 header 없음");
        }

        String resultCode = response.header().resultCode();
        if(!"00".equals(resultCode)) {
            throw new IllegalStateException("OpenAPI 오류: resultCode=" + resultCode + ", msg=" + response.header().resultMsg());
        }
        if(response.body() == null) {
            throw new IllegalStateException("응답 파싱 실패: body 없음");
        }

        return response.body();
    }
}
