
CREATE TABLE IF NOT EXISTS lawd_code (
    lawd10           CHAR(10)     NOT NULL COMMENT '법정동코드(10자리) PK',
    name             VARCHAR(200) NOT NULL COMMENT '법정동명(지역주소명/최하위지역명 포함)',
    sido_cd          CHAR(2)               COMMENT '시도코드(코드 분해)',
    sgg_cd3          CHAR(3)               COMMENT '시군구코드(코드 분해)',
    emd_cd3          CHAR(3)               COMMENT '읍면동코드(코드 분해)',
    li_cd2           CHAR(2)               COMMENT '리코드(코드 분해)',
    upper_code       CHAR(10)              COMMENT '상위지역코드',
    resident_code    CHAR(10)              COMMENT '지역코드_주민(주민등록)',
    cadastral_code   CHAR(10)              COMMENT '지역코드_지적(지적전산)',
    addr_name        VARCHAR(300)          COMMENT '지역주소명(전체 경로형)',
    level_order      INT                   COMMENT '서열/레벨',
    create_date      CHAR(8)               COMMENT '생성일(YYYYMMDD)',
    abolish_date     CHAR(8)               COMMENT '폐지일(YYYYMMDD)',
    abolish_flag     VARCHAR(10)           COMMENT '폐지구분(현존/폐지 등)',
    remark           VARCHAR(300)          COMMENT '비고',
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (lawd10),
    KEY idx_lawd_name (name),
    KEY idx_upper (upper_code),
    KEY idx_sido_sgg_emd_li (sido_cd, sgg_cd3, emd_cd3, li_cd2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE lawd_code
    ADD COLUMN lawd5 CHAR(5)
    AS (LEFT(lawd10, 5)) PERSISTENT;

-- lawd5 전용 인덱스(검색/정렬 최적화)
CREATE INDEX idx_lawd5 ON lawd_code(lawd5);

-- “현존”만 빠르게 뽑고 싶다면(선택): 부분 인덱스 불가 → 플래그 컬럼에 일반 인덱스 권장
CREATE INDEX idx_lawd_abolish ON lawd_code(abolish_flag);

-- 조회용 뷰(중복 제거 + “현존” 우선 규칙)
CREATE OR REPLACE VIEW v_lawd_code_sgg AS
SELECT t.*
FROM (
     SELECT
         lawd5,
         MIN(CASE WHEN (abolish_flag IS NULL OR abolish_flag NOT LIKE '%폐지%') THEN 0 ELSE 1 END) AS alive_rank,
         MIN(name) AS sgg_nm   -- 동일 lawd5 다수 이름이 있으면 사전식 최소값 선택(원하면 다른 규칙으로)
     FROM lawd_code
     GROUP BY lawd5
 ) x    -- ← 여기서 x가 서브쿼리의 alias (별칭)
     JOIN lawd_code t
          ON t.lawd5 = x.lawd5
              AND ( (t.abolish_flag IS NULL OR t.abolish_flag NOT LIKE '%폐지%') = (x.alive_rank = 0) )
              AND t.name = x.sgg_nm;


