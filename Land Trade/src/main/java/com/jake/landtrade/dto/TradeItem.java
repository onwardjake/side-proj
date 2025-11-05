package com.jake.landtrade.dto;

import jakarta.validation.constraints.NotBlank;

public record TradeItem(
        String sggCd,
        String umdCd,
        String umdNm,
        String jibun,
        String landCd,
        String bonbun,
        String bubun,

        String roadNm,
        String roadNmSggCd,
        String roadNmCd,
        String roadNmSeq,
        String roadNmbCd,
        String roadNmBonbun,
        String roadNmBubun,

        @NotBlank String aptNm,
        String aptSeq,
        String aptDong,
        String floor,
        String excluUseAr,
        String buildYear,

        String dealYear,
        String dealMonth,
        String dealDay,
        String dealAmount,      // "123,456" 형태(만원)

        String rgstDate,
        String dealingGbn,
        String estateAgentSggNm,
        String slerGbn,
        String buyerGbn,
        String landLeaseholdGbn,
        String cdealType,
        String cdealDay
) {
}
