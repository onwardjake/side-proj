package com.jake.landtrade.mapper;

import com.jake.landtrade.dto.lawd.LawdItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LawdCodeMapper {
    // TABLE: lawd_code_sgg
    List<String> selectAllLawdCdSgg();

    int upsertAll(@Param("list") List<LawdItem> list);
}
