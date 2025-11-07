package com.jake.landtrade.util;

import com.jake.landtrade.dto.OpenApiTradeItem;
import com.jake.landtrade.dto.TradeItem;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AptTradeMapperUtil {
    /**
     * OpenAPI 응답 DTO(OpenApiTradeItem) → DB 저장용 DTO(TradeItem)
     */
    public static TradeItem map(OpenApiTradeItem src) {
        if(src == null) {
            return null;
        }
        try{
            TradeItem t = new TradeItem();

            // ===== 주소 =====
            t.setSggCd(clean(src.getSggCd()));
            t.setUmdCd(clean(src.getUmdCd()));
            t.setUmdNm(clean(src.getUmdNm()));
            t.setJibun(clean(src.getJibun()));
            t.setLandCd(clean(src.getLandCd()));
            t.setBonbun(clean(src.getBonbun()));
            t.setBubun(clean(src.getBubun()));

            // ===== 도로명 =====
            t.setRoadNm(clean(src.getRoadNm()));
            t.setRoadNmSggCd(clean(src.getRoadNmSggCd()));
            t.setRoadNmCd(clean(src.getRoadNmCd()));
            t.setRoadNmSeq(clean(src.getRoadNmSeq()));
            t.setRoadNmbCd(clean(src.getRoadNmbCd()));
            t.setRoadNmBonbun(clean(src.getRoadNmBonbun()));
            t.setRoadNmBubun(clean(src.getRoadNmBubun()));

            // ===== 아파트 =====
            t.setAptNm(clean(src.getAptNm()));
            t.setAptSeq(clean(src.getAptSeq()));
            t.setAptDong(clean(src.getAptDong()));
            t.setFloor(parseInt(src.getFloor()));
            t.setExcluUseAr(parseFloat(src.getExcluUseAr())); // float 사용
            t.setBuildYear(parseInt(src.getBuildYear()));

            // ===== 계약 =====
            t.setDealYear(parseInt(src.getDealYear()));
            t.setDealMonth(parseInt(src.getDealMonth()));
            t.setDealDay(parseInt(src.getDealDay()));
            t.setDealAmount(parseInt(removeComma(src.getDealAmount())));

            // ===== 기타 =====
            t.setRgstDate(parseDate(src.getRgstDate()));
            t.setDealingGbn(clean(src.getDealingGbn()));
            t.setEstateAgentSggNm(clean(src.getEstateAgentSggNm()));
            t.setSlerGbn(clean(src.getSlerGbn()));
            t.setBuyerGbn(clean(src.getBuyerGbn()));
            t.setLandLeaseholdGbn(clean(src.getLandLeaseholdGbn()));
            t.setCdealType(clean(src.getCdealType()));
            t.setCdealDay(parseDate(src.getCdealDay()));

            return t;
        } catch (Exception e) {
            log.warn("OpenAPI 응답 DTO -> DB 저장용 DTO 변환 실패: {} => {}", src, e.getMessage());
            return null;
        }
    }


    // ================= 유틸 메서드 =================

    private static String clean(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static String removeComma(String s) {
        if (s == null) return null;
        return s.replace(",", "").replace(" ", "");
    }

    private static Integer parseInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Long parseLong(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Float parseFloat(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDate(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            s = s.trim().replaceAll("[^0-9]", "");
            if (s.length() == 8)
                return LocalDate.parse(s, DateTimeFormatter.BASIC_ISO_DATE);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
