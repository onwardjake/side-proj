package com.jake.landtrade.dto.lawd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.jake.landtrade.dto.ApiResponse;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseLawd {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode") private String resultCode;
        @JsonProperty("resultMsg") private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        // 핵심 키
        @JsonProperty("법정동코드")      private String code10;
        @JsonProperty("법정동명")        private String name;

        // 세부 분해/메타 (소스에 있으면 수신)
        @JsonProperty("시도코드")        private String sidoCd;
        @JsonProperty("시군구코드")      private String sggCd3;
        @JsonProperty("읍면동코드")      private String emdCd3;
        @JsonProperty("리코드")          private String liCd2;

        @JsonProperty("상위지역코드")    private String upperCode;
        @JsonProperty("지역코드_주민")    private String residentCode;
        @JsonProperty("지역코드_지적")    private String cadastralCode;

        @JsonProperty("지역주소명")      private String addrName;
        @JsonProperty("서열")           private Integer levelOrder;
        @JsonProperty("비고")           private String remark;

        @JsonProperty("생성일")          private String createDate;   // YYYYMMDD
        @JsonProperty("폐지일")          private String abolishDate;  // YYYYMMDD
        @JsonProperty("폐지구분")        private String abolishFlag;  // 현존/폐지 등 텍스트
        @JsonProperty("최하위지역명")    private String leafName;     // 필요시 name와 병행 사용
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName="item")
        @JsonProperty("item")
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("items") private Items items;
        @JsonProperty("numOfRows") private Integer numOfRows;
        @JsonProperty("pageNo") private Integer pageNo;
        @JsonProperty("totalCount") private Integer totalCount;
    }

    @JsonProperty("header") private Header header;
    @JsonProperty("body") private Body body;
}
