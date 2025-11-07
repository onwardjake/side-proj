package com.jake.landtrade.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LawdCodeMapper {
    // TABLE: lawd_code_sgg
    @Select("SELECT lawd_cd_sgg FROM lawd_code_sgg ORDER BY lawd_cd_sgg ASC")
    List<String> selectAllLawdCdSgg();
}
