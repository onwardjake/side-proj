package com.jake.landtrade.dto;

public record Body(
        Items items,
        Integer numOfRows,
        Integer pageNo,
        Integer totalCount
) {}
