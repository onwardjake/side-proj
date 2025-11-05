package com.jake.landtrade.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record Header(
        @JacksonXmlProperty(localName = "resultCode") String resultCode,
        @JacksonXmlProperty(localName = "resultMsg") String resultMsg
) {}
