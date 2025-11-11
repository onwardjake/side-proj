package com.jake.landtrade.client;

import com.jake.landtrade.config.OpenApiPropsApt;
import com.jake.landtrade.config.OpenApiPropsLawd;
import com.jake.landtrade.dto.ApiResponse;
import com.jake.landtrade.dto.Body;
import com.jake.landtrade.dto.OpenApiTradeItem;
import com.jake.landtrade.dto.lawd.ApiResponseLawd;
import com.jake.landtrade.dto.lawd.LawdItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LawdCdOpenApiClient extends OpenApiClient<OpenApiPropsLawd> {
    public LawdCdOpenApiClient(OpenApiPropsLawd props){
        super(props);
    }

    public ApiResponseLawd fetchPage(Integer pageNo, Integer pageSize) {
        String raw = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(props.servicePath())
                        .queryParam("serviceKey", urlEncode(props.serviceKey()))
                        .queryParam("pageNo", Optional.ofNullable(pageNo).orElse(1))
                        .queryParam("numOfRows", Optional.ofNullable(pageSize).orElse(props.defaultPageSize()))
                        .build(true))
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::map4xx)
                .onStatus(HttpStatusCode::is5xxServerError, this::map5xx)
                .bodyToMono(String.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException("OpenAPI 호출 실패: " + ex.getMessage(), ex)))
                .block();

        // 원문 XML 로그(원할 때만)
        log.debug("RAW XML = {}", raw);

        try {
            return xmlMapper.readValue(raw, ApiResponseLawd.class);     // ← Jackson XmlMapper로 파싱
        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 실패: " + e.getMessage(), e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : fetchAll
    // description : 페이지 개수 만큼 fetchPage를 호출하여 전체 데이터를 받는다.
    // parameter : lawdCd - 법정동코드 5자리(ex. 11110), dealYmd - 거래년월(ex. 202510)
    // return type : 받아온 실거래가 데이터를 TradeItem 리스트로 만들어 리턴한다
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public List<ApiResponseLawd.Items> fetchAll() {
        int page = 1;
        int rows = props.defaultPageSize();

        ApiResponseLawd first = fetchPage(page, rows);
        ApiResponseLawd.Body body = ensureSuccess(first);

        int total = Optional.ofNullable(body.totalCount()).orElse(0);
        List<ApiResponseLawd.Item> all = new ArrayList<>();
        if(body.items() != null && body.items().item() != null) {
            all.addAll(body.items().item());
        }

        int pageCount = (int) Math.ceil(total/(double)rows);
        for(page = 2; page <= pageCount; page++) {
            ApiResponse resp = fetchPage(page, rows);
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
    public static Body ensureSuccess(ApiResponseLawd response) {
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
