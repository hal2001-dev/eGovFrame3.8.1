-- =====================================================================
--  Oracle DDL for the eGovFrame sample (run once on your Oracle schema).
--  The app does NOT auto-create these when the 'oracle' profile is active.
-- =====================================================================

CREATE TABLE SAMPLE (
    ID          VARCHAR2(20)   NOT NULL,
    NAME        VARCHAR2(100),
    DESCRIPTION VARCHAR2(4000),
    USE_YN      CHAR(1)        DEFAULT 'Y',
    REG_USER    VARCHAR2(50),
    CONSTRAINT PK_SAMPLE PRIMARY KEY (ID)
);

-- REST API clients (HMAC credentials)
CREATE TABLE API_CLIENT (
    API_KEY     VARCHAR2(50)   NOT NULL,
    SECRET      VARCHAR2(200)  NOT NULL,
    USE_YN      CHAR(1)        DEFAULT 'Y',
    CLIENT_NAME VARCHAR2(100),
    CONSTRAINT PK_API_CLIENT PRIMARY KEY (API_KEY)
);

-- allowed source IP ranges per client (CIDR); no rows = no IP restriction
CREATE TABLE API_CLIENT_IP (
    API_KEY VARCHAR2(50) NOT NULL,
    CIDR    VARCHAR2(50) NOT NULL,
    CONSTRAINT PK_API_CLIENT_IP PRIMARY KEY (API_KEY, CIDR),
    CONSTRAINT FK_API_CLIENT_IP FOREIGN KEY (API_KEY) REFERENCES API_CLIENT (API_KEY)
);
