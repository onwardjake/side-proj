package com.jake.landtrade.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jake.landtrade.config.OpenApiProps;
import com.jake.landtrade.dto.ApiResponseTradeItem;
import com.jake.landtrade.dto.Body;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OpenApiClient {
    protected final WebClient webClient;
    protected final OpenApiProps props;
    protected final XmlMapper xmlMapper = new XmlMapper();

    public OpenApiClient(OpenApiProps props){
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
    // method : ensureSuccess
    // description : API 호출에 대한 응답에 문제가 없는지 확인하고, 문제가 없다면 repose의 body를 리턴한다.
    // parameter : API 호출에 대한 결과가 들어있는 response 객체
    // return type : response 객체의 body를 리턴한다.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected static Body ensureSuccess(ApiResponseTradeItem response) {
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

    protected static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("WebClient 요청: {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    protected static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.info("WebClient 응답: status={}", res.statusCode());
            return Mono.just(res);
        });
    }

    protected <T> Mono<? extends Throwable> map4xx(ClientResponse resp) {
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

    protected <T> Mono<? extends Throwable> map5xx(ClientResponse resp) {
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

    protected static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    protected static String summarize(String body) {
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }

    protected boolean isRetryable(Throwable t) {
        // 5xx, 타임아웃, 커넥션 등은 재시도 함
        // 4xx 등 클라이언트 오류는 재시도 안 함
        if(t instanceof WebClientResponseException wex) {
            return wex.getStatusCode().is5xxServerError();
        }
        return true;
    }
}
