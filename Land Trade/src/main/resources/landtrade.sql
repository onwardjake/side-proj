
-- DB 생성 (문자셋/콜레이션은 한글 안전하게 utf8mb4 권장)
CREATE DATABASE IF NOT EXISTS landtrade
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE landtrade;

-- 재사용 가능한 정리 함수
-- cleanup_text 특수문자/제로폭 제거 함수
DROP FUNCTION IF EXISTS cleanup_text;
DELIMITER $$
CREATE FUNCTION cleanup_text(s TEXT)
    RETURNS VARCHAR(1024)
        CHARACTER SET utf8mb4
        COLLATE utf8mb4_general_ci
BEGIN
RETURN TRIM(
        REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
            s,
            CHAR(13), ''),       -- \r
            CHAR(10), ''),       -- \n
            CHAR(9),  ' '),      -- \t
            CHAR(160),' '),      -- NBSP
            UNHEX('E2808B'), ''),-- U+200B
            UNHEX('EFBBBF'), '') -- U+FEFF
       );
END $$
DELIMITER ;

-- 아파트 실거래 데이터 저장 테이블
DROP TABLE IF EXISTS apt_trade;
CREATE TABLE apt_trade (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'PK',

    -- 주소 정보
    sgg_cd            CHAR(5)        NOT NULL COMMENT '법정동시군구코드(= LAWD_CD 대응)', -- 11140
    umd_cd            CHAR(5)            NULL COMMENT '법정동읍면동코드',                -- 17400
    umd_nm            VARCHAR(100)       NULL COMMENT '법정동명',           -- 만리동2가
    jibun             VARCHAR(50)        NULL COMMENT '지번',             -- 273
    land_cd           VARCHAR(10)        NULL COMMENT '법정동지번코드',        -- 1
    bonbun            VARCHAR(10)        NULL COMMENT '법정동본번코드',        -- 0273
    bubun             VARCHAR(10)        NULL COMMENT '법정동부번코드',        -- 0000

    -- 도로명 주소
    road_nm           VARCHAR(200)       NULL COMMENT '도로명',              -- 만리재로
    road_nm_sgg_cd    CHAR(5)            NULL COMMENT '도로명시군구코드',       -- 11140
    road_nm_cd        VARCHAR(20)        NULL COMMENT '도로명코드',            -- 3101016
    road_nm_seq       VARCHAR(10)        NULL COMMENT '도로명일련번호코드',      -- 02
    road_nmb_cd       CHAR(1)            NULL COMMENT '도로명지상지하코드',      -- 0
    road_nm_bonbun    VARCHAR(10)        NULL COMMENT '도로명건물본번호코드',     -- 00175
    road_nm_bubun     VARCHAR(10)        NULL COMMENT '도로명건물부번호코드',     -- 00000

    -- 아파트 정보 (단지/면적/층/건축년도)
    apt_nm            VARCHAR(200)   NOT NULL COMMENT '아파트명',           -- 서울역센트럴자이
    apt_seq           VARCHAR(12)        NULL COMMENT '단지 일련번호',       -- 11140-1300
    apt_dong          VARCHAR(50)        NULL COMMENT '아파트 동명',
    floor             SMALLINT           NULL COMMENT '층',               -- 4
    exclu_use_ar      DECIMAL(10,4)  NOT NULL COMMENT '전용면적(m2)',       -- 89.8989
    build_year        SMALLINT           NULL COMMENT '건축년도(YYYY)',     -- 2017

    -- 계약 정보
    deal_year         SMALLINT       NOT NULL COMMENT '계약년도(YYYY)',     -- 2025
    deal_month        TINYINT        NOT NULL COMMENT '계약월(1-12)',      -- 10
    deal_day          TINYINT        NOT NULL COMMENT '계약일(1-31)',      -- 9
    deal_amount       INT            NOT NULL COMMENT '거래금액(만원)',      -- 235,000

    -- 거래 상태/유형/기타
    rgst_date         DATE               NULL COMMENT '등기일자',
    dealing_gbn       VARCHAR(30)        NULL COMMENT '거래유형(중개/직거래 등)',     -- 중개거래
    estate_agent_sgg_nm VARCHAR(100)     NULL COMMENT '중개사소재지(시군구)',         -- 서울 중구
    sler_gbn          VARCHAR(20)        NULL COMMENT '거래주체_매도자(개인/법인/공공/기타)',
    buyer_gbn         VARCHAR(20)        NULL COMMENT '거래주체_매수자(개인/법인/공공/기타)',  -- 개인
    land_leasehold_gbn VARCHAR(10)       NULL COMMENT '토지임대부 여부',       -- N
    cdeal_type        VARCHAR(20)        NULL COMMENT '해제여부',
    cdeal_day         DATE               NULL COMMENT '해제사유발생일',

    -- 관리
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY(id),

    -- 중복 방지용 자연키
    -- 동일 단지/면적/층/계약일(연-월-일) 기준으로 유니크 처리
    UNIQUE KEY uq_trade_main(
        sgg_cd, apt_nm, exclu_use_ar, build_year, floor, deal_year, deal_month, deal_day
    ),

    -- 조회 성능용 인덱스
    KEY idx_lawd (sgg_cd),
    KEY idx_deal_ym (deal_year, deal_month),
    KEY idx_build_year (build_year),
    KEY idx_apt (apt_nm),
    KEY idx_umd_cd (umd_cd),
    KEY idx_umd_nm (umd_nm)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 지역코드 테이블
DROP TABLE IF EXISTS lawd_code;
CREATE TABLE lawd_code
(
);

-- 지역코드에서 앞 5자리로 구성된 시군구만 추출한 테이블 (중복 제거)
-- 1114016200(서울 중구 신당동), 1114017400(서울특별시 중구 만리동2가) -> 11140(서울시 중구) 1개 데이터만 입력
DROP TABLE IF EXISTS lawd_code_sgg;
CREATE TABLE lawd_code_sgg
(
);
