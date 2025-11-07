package com.jake.landtrade.service;

import com.jake.landtrade.client.LawdCdOpenApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LawdCdService {
    private final LawdCdOpenApiClient lawdCdOpenApiClient;
    public void getAllLawdCds() {
        lawdCdOpenApiClient.fetchAll();
    }
}
