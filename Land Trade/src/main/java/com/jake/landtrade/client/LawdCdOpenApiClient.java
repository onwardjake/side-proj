package com.jake.landtrade.client;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jake.landtrade.config.OpenApiProps;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LawdCdOpenApiClient extends OpenApiClient {
    public LawdCdOpenApiClient(OpenApiProps props){
        super(props);
    }

    //public List<LawdItem> fetchAll() throws IOException {
    public void fetchAll() {
        /*
        try {
            String finalKey = "2b5e4fdb83723904417206dd7c71aac959683fdc1fc50981d15ac54e46b3d933";
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1741000/StanReginCd/getStanReginCdList"); // URL
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + finalKey); // Service Key
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); // 페이지번호
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("20", "UTF-8")); // 한 페이지 결과 수
            urlBuilder.append("&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); // 호출문서(xml, json) default : xml
            //urlBuilder.append("&" + URLEncoder.encode("locatadd_nm","UTF-8") + "=" + URLEncoder.encode("서울특별시", "UTF-8")); // 지역주소명
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.getMessage();
        }
        //*/
    }
}
