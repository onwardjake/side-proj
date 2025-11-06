package com.jake.landtrade.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jake.landtrade.config.OpenApiProps;
import com.jake.landtrade.dto.ApiResponse;
import com.jake.landtrade.dto.TradeItem;
import com.jake.landtrade.dto.Body;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
// + 기존 import 유지


@Slf4j
@Component
public class AptTradeOpenApiClient {
    private final WebClient webClient;
    private final OpenApiProps props;
    private final XmlMapper xmlMapper = new XmlMapper();

    public AptTradeOpenApiClient(OpenApiProps props){
        this.props = props;

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutMills())
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(props.responseTimeoutMills(), TimeUnit.MILLISECONDS)))
                .responseTimeout(Duration.ofMillis(props.responseTimeoutMills()));

        // MVC 환경에서도 WebClient를 순수 클라이언트로 사용
        this.webClient = WebClient.builder()
                .baseUrl(props.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(h -> h.setAccept(List.of(MediaType.APPLICATION_XML)))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // method : fetchPage
    // description : 페이지 단위로 Open API를 호출하여 결과를 받아온다.
    // parameter : lawdCd - 법정동코드 5자리(ex. 11110), dealYmd - 거래년월(ex. 202510)
    //             pageNo - 요청 페이지 번호, pageSize - 페이지당 항목 개수
    // return type : 받아온 실거래가 데이터를 TradeItem 리스트로 만들어 리턴한다
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ApiResponse fetchPage(String lawdCd, String dealYmd, Integer pageNo, Integer pageSize){
        /* URL 디버그용
        String finalKey = urlEncode(props.serviceKey());
        String url = UriComponentsBuilder.fromUriString(props.baseUrl())
                .path(props.servicePath())
                .queryParam("LAWD_CD", lawdCd)
                .queryParam("DEAL_YMD", dealYmd)
                .queryParam("serviceKey", finalKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", pageSize)
                .build(true) // 이미 인코딩된 파라미터를 보존
                .toUriString();

        System.out.println("[DEBUG] calling URL = " + url);
        System.out.println(urlEncode(props.serviceKey()));
        //*/

        /*
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(props.servicePath())
                        .queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
                        .queryParam("serviceKey", urlEncode(props.serviceKey()))
                        .queryParam("pageNo", Optional.ofNullable(pageNo).orElse(1))
                        .queryParam("numOfRows", Optional.ofNullable(pageSize).orElse(props.defaultPageSize()))
                        .build(true))
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::map4xx)
                .onStatus(HttpStatusCode::is5xxServerError, this::map5xx)
                .bodyToMono(ApiResponse.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException("OpenAPI 호출 실패: " + ex.getMessage(), ex)))
                .block();
        //*/

        String raw = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(props.servicePath())
                        .queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
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
            return xmlMapper.readValue(raw, ApiResponse.class);     // ← Jackson XmlMapper로 파싱
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
    public List<TradeItem> fetchAll(String lawdCd, String dealYmd) {
        int page = 1;
        int rows = props.defaultPageSize();

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

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("WebClient 요청: {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.info("WebClient 응답: status={}", res.statusCode());
            return Mono.just(res);
        });
    }

    private <T> Mono<? extends Throwable> map4xx(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> {
                    int code = resp.statusCode().value();
                    String msg = "OpenAPI 4xx 오류: " + code + " - " + summarize(body);
                    log.warn(msg);
                    return new WebClientResponseException(
                            msg,
                            resp.statusCode().value(),
                            HttpStatus.resolve(code) != null ? HttpStatus.resolve(code).name() : "CLIENT_ERROR",
                            resp.headers().asHttpHeaders(),
                            body.getBytes(),
                            null);
                });
    }

    private <T> Mono<? extends Throwable> map5xx(ClientResponse resp) {
        return resp.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> {
                    int code = resp.statusCode().value();
                    String msg = "OpenAPI 5xx 오류: " + code + " - " + summarize(body);
                    log.error(msg);
                    return new WebClientResponseException(
                            msg,
                            resp.statusCode().value(),
                            HttpStatus.resolve(code) != null ? HttpStatus.resolve(code).name() : "SERVER_ERROR",
                            resp.headers().asHttpHeaders(),
                            body.getBytes(),
                            null);
                });
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String summarize(String body) {
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }

    private boolean isRetryable(Throwable t) {
        // 5xx, 타임아웃, 커넥션 등은 재시도 함
        // 4xx 등 클라이언트 오류는 재시도 안 함
        if(t instanceof WebClientResponseException wex) {
            return wex.getStatusCode().is5xxServerError();
        }
        return true;
    }
}
