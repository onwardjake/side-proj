package com.jake.landtrade.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * 국토부_아파트매매 실거래가 상세 XML 응답 item 매핑용 DTO
 */
@Data
public class OpenApiTradeItem {
    // 주소
    @JacksonXmlProperty(localName="sggCd")
    private String sggCd;

    @JacksonXmlProperty(localName = "umdCd")
    private String umdCd;

    @JacksonXmlProperty(localName = "umdNm")
    private String umdNm;

    @JacksonXmlProperty(localName = "jibun")
    private String jibun;

    @JacksonXmlProperty(localName = "landCd")
    private String landCd;

    @JacksonXmlProperty(localName = "bonbun")
    private String bonbun;

    @JacksonXmlProperty(localName = "bubun")
    private String bubun;

    // ================== 도로명 정보 ==================
    @JacksonXmlProperty(localName = "roadNm")
    private String roadNm;

    @JacksonXmlProperty(localName = "roadNmSggCd")
    private String roadNmSggCd;

    @JacksonXmlProperty(localName = "roadNmCd")
    private String roadNmCd;

    @JacksonXmlProperty(localName = "roadNmSeq")
    private String roadNmSeq;

    @JacksonXmlProperty(localName = "roadNmbCd")
    private String roadNmbCd;

    @JacksonXmlProperty(localName = "roadNmBonbun")
    private String roadNmBonbun;

    @JacksonXmlProperty(localName = "roadNmBubun")
    private String roadNmBubun;

    // ================== 아파트 정보 ==================
    @JacksonXmlProperty(localName = "aptNm")
    private String aptNm;

    @JacksonXmlProperty(localName = "aptSeq")
    private String aptSeq;

    @JacksonXmlProperty(localName = "aptDong")
    private String aptDong;

    @JacksonXmlProperty(localName = "floor")
    private String floor;

    @JacksonXmlProperty(localName = "excluUseAr")
    private String excluUseAr;

    @JacksonXmlProperty(localName = "buildYear")
    private String buildYear;

    // ================== 계약 정보 ==================
    @JacksonXmlProperty(localName = "dealYear")
    private String dealYear;

    @JacksonXmlProperty(localName = "dealMonth")
    private String dealMonth;

    @JacksonXmlProperty(localName = "dealDay")
    private String dealDay;

    @JacksonXmlProperty(localName = "dealAmount")
    private String dealAmount;

    // ================== 거래 상태/기타 ==================
    @JacksonXmlProperty(localName = "rgstDate")
    private String rgstDate;

    @JacksonXmlProperty(localName = "dealingGbn")
    private String dealingGbn;

    @JacksonXmlProperty(localName = "estateAgentSggNm")
    private String estateAgentSggNm;

    @JacksonXmlProperty(localName = "slerGbn")
    private String slerGbn;

    @JacksonXmlProperty(localName = "buyerGbn")
    private String buyerGbn;

    @JacksonXmlProperty(localName = "landLeaseholdGbn")
    private String landLeaseholdGbn;

    @JacksonXmlProperty(localName = "cdealType")
    private String cdealType;

    @JacksonXmlProperty(localName = "cdealDay")
    private String cdealDay;
}
