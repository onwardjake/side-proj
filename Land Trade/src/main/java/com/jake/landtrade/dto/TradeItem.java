package com.jake.landtrade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TradeItem {
    // 주소
    private String sggCd;
    private String umdCd;
    private String umdNm;
    private String jibun;
    private String landCd;
    private String bonbun;
    private String bubun;

    // 도로명 주소
    private String roadNm;
    private String roadNmSggCd;
    private String roadNmCd;
    private String roadNmSeq;
    private String roadNmbCd;
    private String roadNmBonbun;
    private String roadNmBubun;

    // 아파트 정보
    @NotBlank private String aptNm;
    private String aptSeq;
    private String aptDong;
    private Integer floor;
    private Float excluUseAr;
    private Integer buildYear;

    // 계약 정보
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private Integer dealAmount;      // "123;456" 형태(만원)

    // 거래 상태/유형/기타
    private LocalDate rgstDate;
    private String dealingGbn;
    private String estateAgentSggNm;
    private String slerGbn;
    private String buyerGbn;
    private String landLeaseholdGbn;
    private String cdealType;
    private LocalDate cdealDay;
    
    // 관리
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
