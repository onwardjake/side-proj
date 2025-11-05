package com.jake.landtrade.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "response")
public record ApiResponse(
        Header header,
        Body body
) {}
