package com.jake.landtrade.service;

import com.jake.landtrade.client.LawdCdOpenApiClient;
import com.jake.landtrade.mapper.LawdCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LawdCdService {
    private final LawdCdOpenApiClient lawdCdOpenApiClient;
    private final LawdCodeMapper lawdCodeMapper;

    // 초기 적재 controller나 scheduler에서 호출
    @Transactional
    public void initialLoad(long delayMills) {
        log.info("법정동코드 Initial load");

        // open api를 호출해서 데이터를 받아온다
        lawdCdOpenApiClient.

        // 필요 시 DB 필드에 맞게 데이터를 변환한다

        ]// DB에 upsert 한다.
        lawdCodeMapper.upsertAll(list);
    }
}
