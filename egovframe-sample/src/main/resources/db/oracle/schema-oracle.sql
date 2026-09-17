-- =====================================================================
--  Oracle DDL (운영 Oracle 스키마에 1회 실행).
--  oracle 프로파일에서는 앱이 이 테이블을 자동 생성하지 않습니다.
-- =====================================================================

CREATE TABLE SAMPLE (
    ID          VARCHAR2(20)   NOT NULL,
    NAME        VARCHAR2(100),
    DESCRIPTION VARCHAR2(4000),
    USE_YN      CHAR(1)        DEFAULT 'Y',
    REG_USER    VARCHAR2(50),
    CONSTRAINT PK_SAMPLE PRIMARY KEY (ID)
);

-- REST API 클라이언트(HMAC 자격증명)
CREATE TABLE API_CLIENT (
    API_KEY     VARCHAR2(50)   NOT NULL,
    SECRET      VARCHAR2(200)  NOT NULL,
    USE_YN      CHAR(1)        DEFAULT 'Y',
    CLIENT_NAME VARCHAR2(100),
    CONSTRAINT PK_API_CLIENT PRIMARY KEY (API_KEY)
);

-- 클라이언트별 허용 출발지 IP 대역(CIDR). 행이 없으면 IP 제한 없음.
CREATE TABLE API_CLIENT_IP (
    API_KEY VARCHAR2(50) NOT NULL,
    CIDR    VARCHAR2(50) NOT NULL,
    CONSTRAINT PK_API_CLIENT_IP PRIMARY KEY (API_KEY, CIDR),
    CONSTRAINT FK_API_CLIENT_IP FOREIGN KEY (API_KEY) REFERENCES API_CLIENT (API_KEY)
);
