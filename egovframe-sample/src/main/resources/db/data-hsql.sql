-- 게시판 샘플 시드 데이터
INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000001', '전자정부 표준프레임워크', 'eGovFrame 3.8 sample seed data', 'Y', 'admin');
INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000002', 'Spring 4.3 + MyBatis', 'JDK 1.8 based sample', 'Y', 'admin');
INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000003', 'HSQLDB in-memory', 'No external DB required', 'Y', 'admin');

-- REST API 클라이언트(HMAC). 실제 사용 전에 비밀키(SECRET)를 반드시 교체하세요.
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('EXTSYS001', 'change-me-external-system-secret-0001', 'Y', 'External System 1');
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('PARTNER-A', 'change-me-partner-a-secret-9f8e7d6c5b4a', 'Y', 'Partner A');
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('DISABLED-X', 'whatever-secret', 'N', 'Disabled sample');

-- 클라이언트별 허용 출발지 IP 대역(CIDR). EXTSYS001 은 행이 없어 IP 제한 없음.
INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('PARTNER-A', '198.51.100.0/24');
INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('PARTNER-A', '203.0.113.10');
