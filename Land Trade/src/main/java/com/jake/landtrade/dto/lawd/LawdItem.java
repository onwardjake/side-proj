package com.jake.landtrade.dto.lawd;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LawdItem {
    private String code10;
    private String name;
    private String sidoCd;
    private String sggCd3;
    private String emdCd3;
    private String liCd2;
    private String upperCode;
    private String residentCode;
    private String cadastralCode;
    private String addrName;
    private Integer levelOrder;
    private String createDate;
    private String abolishDate;
    private String abolishFlag;
    private String remark;
}
