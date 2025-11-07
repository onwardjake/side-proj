package com.jake.landtrade.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "response")
public record ApiResponseTradeItem(
        Header header,
        Body body
) {}
