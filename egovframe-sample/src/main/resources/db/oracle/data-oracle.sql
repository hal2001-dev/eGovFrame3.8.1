-- =====================================================================
--  Optional seed data for Oracle. CHANGE the secrets before real use.
-- =====================================================================

INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000001', '전자정부 표준프레임워크', 'eGovFrame 3.8 sample seed data', 'Y', 'admin');
INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000002', 'Spring 4.3 + MyBatis', 'JDK 1.8 based sample', 'Y', 'admin');
INSERT INTO SAMPLE (ID, NAME, DESCRIPTION, USE_YN, REG_USER) VALUES ('1000000003', 'Oracle backend', 'Managed in Oracle', 'Y', 'admin');

INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('EXTSYS001', 'change-me-external-system-secret-0001', 'Y', 'External System 1');
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('PARTNER-A', 'change-me-partner-a-secret-9f8e7d6c5b4a', 'Y', 'Partner A');

INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('PARTNER-A', '198.51.100.0/24');
INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('PARTNER-A', '203.0.113.10');

COMMIT;
