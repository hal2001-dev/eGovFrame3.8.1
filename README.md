# eGovFrame 3.8 Sample (JDK 1.8) — 오프라인 패키지

전자정부 표준프레임워크 **3.8** 실행환경(RTE 3.8.0, Spring 4.3.16, MyBatis 3.4)
기반의 샘플 웹 프로젝트입니다. **인터넷 연결 없이** 빌드/실행할 수 있도록
필요한 모든 의존성 JAR을 `m2-repo/` 에 함께 담았습니다.

샘플 기능: 게시판(Sample) **목록 / 검색 / 등록 / 수정 / 삭제** CRUD
(JSP 화면 + **REST API** 둘 다 제공 — 아래 4-1 참고).
DB는 **HSQLDB 인메모리**를 사용하므로 별도 DB 설치가 필요 없습니다
(기동 시 테이블 생성 + 시드 데이터 3건 자동 적재).

---

## 1. 구성물

```
export/
├── egovframe-sample/      # 프로젝트 소스 (Maven war 프로젝트)
│   ├── pom.xml
│   ├── src/main/java/     # Controller / Service / Mapper / VO
│   ├── src/main/resources/# Spring 설정, MyBatis 매퍼, log4j2, 초기 SQL
│   └── src/main/webapp/   # web.xml, JSP
├── m2-repo/               # 오프라인 로컬 Maven 저장소 (필요 JAR 전체)
├── tools/apache-maven-3.9.16/  # 번들 Maven (설치 불필요, 플랫폼 무관)
├── settings.xml           # eGovFrame 저장소용 Maven 설정 (온라인 재빌드 시에만 필요)
├── build-offline.sh / .bat # 오프라인 빌드 → WAR 생성  (mac·linux / Windows)
├── run-offline.sh  / .bat # 오프라인 실행 → 내장 Jetty (mac·linux / Windows)
├── sign-request.sh        # HMAC 서명 요청 생성/호출 (외부 시스템 참고 구현)
└── README.md
```

REST API는 **HMAC 서명 인증**(서버-투-서버)으로 보호됩니다 — 아래 4-2 참고.

## 2. 요구사항

- **JDK 1.8** (필수, 유일한 사전설치 항목) — 실행 대상 PC의 **OS/아키텍처에 맞는** JDK 8.
  - Windows: Azul Zulu 8 (Windows x64) 또는 Temurin 8 — 예: `C:\Program Files\Zulu\zulu-8`
  - macOS(Apple Silicon): Zulu 8 aarch64
- **Maven 불필요** — Maven 배포본이 `tools/apache-maven-3.9.16/` 에 **번들**되어 있습니다.
- **인터넷 불필요** — 모든 의존 JAR이 `m2-repo/` 에 포함(플랫폼 무관).
  - 즉 **오프라인 PC에는 JDK 1.8만 준비**하면 됩니다.

## 3. 오프라인 빌드 (WAR 생성)

```bash
export JAVA_HOME=/path/to/jdk1.8      # JDK 1.8 경로 지정
./build-offline.sh
```

- 내부적으로 `mvn -o -Dmaven.repo.local=./m2-repo clean package` 를 실행합니다.
  (`-o` = 오프라인 모드, 인터넷 접속 안 함)
- 결과물: `egovframe-sample/target/egovframe-sample.war`
- 이 WAR를 Tomcat 8.5/9 (Servlet 3.1, JDK 8) 등에 배포하면 됩니다.

## 4. 오프라인 실행 (바로 확인)

```bash
export JAVA_HOME=/path/to/jdk1.8
./run-offline.sh
```

- 내장 **Jetty**가 뜹니다. 브라우저에서 접속:
  - 목록: <http://localhost:8080/egovSampleList.do>
  - 등록: <http://localhost:8080/addSample.do>
- 중지: `Ctrl + C`

## 4-0. Windows 에서 (오프라인)

Windows에서는 `.bat` 스크립트를 쓰면 됩니다. **JDK 1.8(Windows x64)만** 준비하면
Maven 설치·인터넷 없이 그대로 빌드/실행됩니다(번들 Maven + `m2-repo` 사용).

명령 프롬프트(cmd) 기준:
```bat
rem 1) JDK 1.8 경로 지정 (본인 설치 경로로)
set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"

rem 2) 빌드 → egovframe-sample\target\egovframe-sample.war
build-offline.bat

rem 3) 실행 → http://localhost:8080/  (Ctrl+C 로 중지)
run-offline.bat

rem Oracle 로 실행할 때 (인자 그대로 전달됨)
run-offline.bat -Dspring.profiles.active=oracle
```

- JDK 8(Windows) 다운로드: Azul Zulu (<https://www.azul.com/downloads/?version=java-8-lts&os=windows&architecture=x86-64-bit&package=jdk>)
  또는 Adoptium Temurin 8. 설치 후 위처럼 `JAVA_HOME` 만 지정하세요.
- 오프라인 PC로 옮길 때는 이 폴더 전체(또는 `git clone`)를 복사 + **JDK 8만** 설치하면 끝.
- PowerShell 이라면 `set` 대신 `$env:JAVA_HOME="C:\Program Files\Zulu\zulu-8"` 로 지정하고
  `.\build-offline.bat` 실행.

## 4-1. REST API (JSON)

JSP 화면과 별개로 동일한 Sample 데이터에 대한 **REST API**가 포함되어 있습니다.
(`EgovSampleApiController`, Jackson JSON 변환, CORS 허용)

| Method | URL | 설명 | 응답 |
|--------|-----|------|------|
| GET | `/api/samples` | 목록 (`?searchCondition=0|1&searchKeyword=`) | `{ "totalCount": n, "list": [...] }` |
| GET | `/api/samples/{id}` | 단건 조회 | `SampleVO` (없으면 404) |
| POST | `/api/samples` | 생성 | 201 + 생성된 리소스 + `Location` 헤더 |
| PUT | `/api/samples/{id}` | 수정 | 수정된 `SampleVO` (없으면 404) |
| DELETE | `/api/samples/{id}` | 삭제 | 204 (없으면 404) |

호출 예시 (서버 기동 후):

```bash
# 목록
curl http://localhost:8080/api/samples

# 생성
curl -X POST http://localhost:8080/api/samples \
  -H "Content-Type: application/json" \
  -d '{"name":"신규","description":"내용","useYn":"Y","regUser":"api"}'

# 단건 / 수정 / 삭제
curl http://localhost:8080/api/samples/{id}
curl -X PUT http://localhost:8080/api/samples/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"수정","description":"변경","useYn":"N","regUser":"api"}'
curl -X DELETE http://localhost:8080/api/samples/{id}
```

## 4-2. 인증 (HMAC 서명 · 서버-투-서버)

REST API(`/api/*`)는 **HMAC-SHA256 서명 인증**으로 보호됩니다
(`HmacAuthFilter`, `web.xml`에서 `/api/*` 에 매핑). 로그인/세션이 아니라
외부 시스템이 **API Key + Secret**으로 매 요청을 서명하는 방식입니다.
JSP 화면은 데모용이라 인증 없이 열려 있습니다(운영 시 별도 보호 권장).

**요청 헤더 3개**

| 헤더 | 값 |
|------|-----|
| `X-API-KEY` | 클라이언트 식별자 (Secret 조회 키) |
| `X-API-TIMESTAMP` | 요청 시각, **epoch milliseconds** |
| `X-API-SIGNATURE` | `HMAC-SHA256(secret, stringToSign)` 의 소문자 hex |

**서명 대상 문자열 (stringToSign)** — 각 줄을 `\n` 로 연결:

```
METHOD                     (예: GET, POST)
PATH                       (예: /api/samples/123, 컨텍스트패스 포함)
QUERY                      (raw query string, 없으면 빈 문자열)
TIMESTAMP                  (X-API-TIMESTAMP 와 동일 값)
SHA-256(body) 의 소문자 hex  (body 없으면 빈 문자열의 해시)
```

**검증 규칙**
- 헤더 누락 / 미등록 키 / 서명 불일치 → **401**
- `X-API-TIMESTAMP` 가 서버 시각과 **±300초(기본)** 초과로 벌어지면 → **401** (재전송 방지)
- 서명 비교는 상수시간 비교

**클라이언트 자격증명은 DB에서 관리** — API Key / Secret / 허용 IP는
`hmac.properties`가 아니라 **데이터베이스 테이블**에 있습니다. 재배포 없이
키 발급·폐기(`USE_YN`)·IP 변경이 가능하고, 변경은 캐시 TTL(기본 60초) 내 반영됩니다.

| 테이블 | 컬럼 | 설명 |
|--------|------|------|
| `API_CLIENT` | `API_KEY`(PK), `SECRET`, `USE_YN`, `CLIENT_NAME` | 키/시크릿, `USE_YN='N'` 이면 즉시 차단 |
| `API_CLIENT_IP` | `API_KEY`(FK), `CIDR` | 그 키의 허용 출발지 IP(행 없으면 IP 제한 없음) |

```sql
-- 새 외부 시스템 발급
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME)
  VALUES ('EXTSYS002', 'a-long-random-secret', 'Y', 'External System 2');
-- 그 시스템의 출발지 IP 대역 지정 (C클래스 예시)
INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('EXTSYS002', '203.0.113.0/24');
-- 키 폐기
UPDATE API_CLIENT SET USE_YN = 'N' WHERE API_KEY = 'EXTSYS002';
```

> 초기 스키마/시드는 `src/main/resources/db/schema-hsql.sql`, `db/data-hsql.sql` 참고.
> 배포 knob(서명 유효시간·전역 IP·프록시 헤더)만 `src/main/resources/hmac.properties`에 있습니다.
> 실제 운영 DB(Oracle/PostgreSQL 등)로 바꾸면 `context-datasource.xml`의 접속 정보와
> 매퍼 SQL만 해당 DB에 맞게 조정하면 됩니다.

**호출 예시** — 동봉된 `sign-request.sh` 가 위 규칙을 그대로 구현한 참고 구현입니다
(외부 시스템은 이 로직을 각자 언어로 이식하면 됩니다):

```bash
API_KEY=EXTSYS001 SECRET='change-me-external-system-secret-0001' BASE=http://localhost:8080 \
  ./sign-request.sh GET /api/samples
./sign-request.sh POST /api/samples '{"name":"n","description":"d","useYn":"Y","regUser":"api"}'
```

**IP 화이트리스트** — CIDR 기반 출발지 IP 제한(**C 클래스=`/24`**, B=`/16`, 단일 IP=`/32`).
IP 거부는 인증 실패(401)와 구분해 **403**으로 응답합니다. 두 층으로 동작합니다:

- **전역** : `hmac.properties`의 `ip.whitelist`(비우면 제한 없음)
- **API Key별** : `API_CLIENT_IP` 테이블(그 키에 행이 있으면 그 대역만 허용) — DB에서 관리
- 검사 순서: **전역 화이트리스트 → HMAC 서명 → API Key별 화이트리스트**

## 4-3. L4 + 웹서버(프록시) 뒤에 둘 때 — 실제 클라이언트 IP

앞단에 **L4 + 웹서버(Apache/nginx)** 가 있으면 `request.getRemoteAddr()` 에는 **프록시 IP**만
찍혀서 IP 화이트리스트가 무의미해집니다. 그래서 웹서버가 실제 클라이언트 IP를 헤더로
넘겨주고, 앱은 그 헤더를 읽도록 설정합니다.

`hmac.properties`:
```properties
# 웹서버가 설정하는 헤더 이름 (권장: 단일 값 X-Real-IP)
ip.forwardedHeader=X-Real-IP
# X-Forwarded-For(콤마 리스트)를 쓰고 고정 프록시 체인이면, 오른쪽에서 몇 번째를
# 클라이언트로 볼지 지정 (0=맨 오른쪽). 비우면 맨 왼쪽 값을 클라이언트로 사용.
#ip.forwardedFromRight=
```

**nginx 예시** (엣지에서 클라이언트가 보낸 헤더를 덮어써 위조 차단):
```nginx
location / {
    proxy_set_header X-Real-IP        $remote_addr;              # 실제 접속 IP
    proxy_set_header X-Forwarded-For  $proxy_add_x_forwarded_for;
    proxy_pass http://was_backend;
}
```

**Apache 예시** (mod_remoteip / mod_proxy):
```apache
RemoteIPHeader X-Forwarded-For
# 또는 단일 값 전달
RequestHeader set X-Real-IP "%{REMOTE_ADDR}s"
ProxyPass        / http://was_backend/
ProxyPassReverse / http://was_backend/
```

> ⚠️ 포인트
> - 웹서버(엣지)가 **클라이언트가 임의로 보낸 `X-Forwarded-For`/`X-Real-IP` 를 반드시 덮어써야**
>   합니다(안 그러면 클라이언트가 IP를 위조 가능). 그래서 앱 기본값은 신뢰 가능한 단일 값
>   `X-Real-IP` 사용을 권장합니다.
> - **L4가 DSR/TCP 패스스루**로 클라이언트 IP를 웹서버까지 보존하는지 확인하세요. SNAT 방식이면
>   웹서버에도 L4 IP만 보여서, L4 단계에서 IP 화이트리스트를 걸거나 L4가 XFF를 넣도록 해야 합니다.
> - HMAC은 무결성·재전송 방지용이며 본문 암호화가 아닙니다 → 실제 연계는 **HTTPS(TLS)** 위에서.

## 4-4. 운영 DB: Oracle 로 전환

기본은 오프라인 데모용 **HSQLDB(인메모리)** 이고, 운영은 **Oracle** 로 전환할 수 있습니다.
Spring 프로파일로 데이터소스만 바뀌며, **매퍼 SQL은 그대로 재사용**됩니다
(`LIKE ... || ...`, 표준 CRUD 라 Oracle 호환).

**전환 방법**
1. Oracle에 테이블 생성 (앱은 oracle 프로파일에서 DDL을 자동 생성하지 않음):
   ```bash
   sqlplus egov/…@DB @egovframe-sample/src/main/resources/db/oracle/schema-oracle.sql
   sqlplus egov/…@DB @egovframe-sample/src/main/resources/db/oracle/data-oracle.sql   # 선택(시드)
   ```
2. 접속정보 수정 — `src/main/resources/oracle.properties`
   ```properties
   oracle.url=jdbc:oracle:thin:@//DB_HOST:1521/ORCLPDB1
   oracle.username=egov
   oracle.password=change-me
   ```
3. **oracle 프로파일로 기동**:
   ```bash
   # 내장 Jetty
   mvn -o -Dmaven.repo.local=../m2-repo -Dspring.profiles.active=oracle jetty:run
   # WAR 배포 시 (톰캣 등): JVM 옵션에 추가
   #   -Dspring.profiles.active=oracle
   ```

- Oracle JDBC 드라이버 **ojdbc8 21.9.0.0** 이 오프라인 저장소에 포함되어 WAR에 함께 패키징됩니다(JDK 8 호환).
- 프로파일 미지정 시 기본은 `hsql`(`web.xml` 의 `spring.profiles.default`).
- **fail-closed**: oracle 프로파일에서 DB가 안 붙으면 인증 캐시가 비어 API는 401로 거부됩니다(안전 기본값).
- 커넥션 풀은 commons-dbcp(`validationQuery=SELECT 1 FROM DUAL`). 운영에선 필요 시
  Oracle UCP/HikariCP로 교체 가능합니다.

> 표(`SAMPLE`, `API_CLIENT`, `API_CLIENT_IP`)와 인덱스/제약은
> `db/oracle/schema-oracle.sql` 참고. 계정/테이블스페이스는 사이트 표준에 맞게 조정하세요.

## 4-5. 데이터소스 2개 사용 (multi-datasource)

기본 DB(`dataSource`/sampledb) 외에 **두 번째 DB(`dataSource2`/sampledb2)** 를 함께 쓰는 예제가 포함되어 있습니다.
핵심은 **데이터소스마다 4가지를 분리**하는 것입니다.

| 구분 | 기본(primary) | 두 번째(secondary) |
|------|---------------|--------------------|
| DataSource | `dataSource` | `dataSource2` |
| SqlSessionFactory | `sqlSession` (매퍼: `mapper/example/*.xml`) | `sqlSession2` (매퍼: `mapper/secondary/*.xml`) |
| 매퍼 구분 애노테이션 | `@Mapper` (org.apache.ibatis) | `@SecondaryMapper` (커스텀 마커) |
| 트랜잭션 매니저 | `txManager` | `txManager2` |

- 설정 파일: 두 번째 DS 는 **`context-datasource-secondary.xml`** 한 곳에 모아 두었습니다(DataSource+Factory+스캐너+TxManager+AOP).
- **매퍼 구분은 애노테이션으로** 합니다. 두 MapperScannerConfigurer 가 **같은 basePackage(`egovframework.example`)** 를 스캔하지만, primary 는 `annotationClass=@Mapper`, secondary 는 `annotationClass=@SecondaryMapper` 라서 서로 겹치지 않습니다. (패키지를 나눌 필요가 없어짐 → 같은 패키지에 두 DB 매퍼가 섞여도 안전)
  - 마커 애노테이션: `egovframework.example.cmm.annotation.SecondaryMapper`
  - 두 번째 DB 매퍼(`ProductMapper`)에는 `@Mapper` 대신 `@SecondaryMapper` 를 붙입니다.
  - (대안) 패키지로 나누고 싶으면 각 스캐너의 `basePackage` 를 서로 겹치지 않게 지정해도 됩니다.
- **트랜잭션(서비스)도 애노테이션으로 분리**합니다. `<tx:annotation-driven transaction-manager="txManager"/>` 하나만 선언하고,
  - 기본 DB 서비스: `@Transactional` (기본값 = `txManager`)
  - 두 번째 DB 서비스: `@Transactional("txManager2")`
  이렇게 하면 매퍼(@SecondaryMapper)처럼 서비스도 **패키지에 매이지 않고** DS별 트랜잭션 매니저가 지정됩니다. (기존 패키지 기반 AOP 는 제거)
  > 주의: 서로 다른 두 트랜잭션 매니저는 **각각 독립 트랜잭션**입니다. 한 트랜잭션으로 두 DB 를 묶으려면 JTA/XA(분산 트랜잭션)가 별도로 필요합니다.

확인:
```bash
curl http://localhost:8080/action/multidb/demo.do
# → {"db1_sampleCount":3, "db2_productCount":3, "db2_productList":[...]}
```
한 요청에서 두 DB(SAMPLE=기본, PRODUCT=두 번째)를 각각 조회해 합쳐 반환합니다.

> 실제 운영에서 두 번째를 별도 실 DB(예: 또 다른 Oracle)로 쓰려면
> `context-datasource-secondary.xml` 의 `dataSource2` url/driver/계정만 바꾸면 됩니다.
> (지금은 오프라인 데모라 두 번째도 HSQLDB 인메모리 사용)

## 5. IDE(이클립스 / 전자정부 표준프레임워크 개발환경)로 열기

1. `File > Import > Maven > Existing Maven Projects`
2. `egovframe-sample` 폴더 선택 → Finish
3. 오프라인 저장소를 쓰려면 프로젝트/워크스페이스의 Maven `Local Repository`를
   `export/m2-repo` 로 지정하거나, `settings.xml`의 `<localRepository>`를 지정하세요.
   - 또는 실행 구성에 `-o -Dmaven.repo.local=<...>/m2-repo` 추가.

## 6. 주요 버전 / 기술 스택

| 구분 | 버전 |
|------|------|
| eGovFrame RTE | 3.8.0 |
| Spring Framework | 4.3.16.RELEASE |
| MyBatis / mybatis-spring | 3.4.1 / 1.3.0 |
| DB (기본/데모) | HSQLDB 2.3.2 (in-memory) |
| DB (운영 옵션) | Oracle (ojdbc8 21.9.0.0), `spring.profiles.active=oracle` |
| Servlet / JSP | 3.1 / 2.3 (JSTL 1.2) |
| REST(JSON) | Jackson 2.9.10 |
| Logging | Log4j2 2.12.4 (SLF4J 경유) |
| JDK | 1.8 |

## 7. 온라인에서 다시 받고 싶을 때 (참고)

인터넷이 되는 환경에서 저장소를 새로 채우려면:

```bash
mvn -s settings.xml -Dmaven.repo.local=./m2-repo clean package
```

`settings.xml` 은 eGovFrame Maven 저장소(`https://maven.egovframe.go.kr/maven/`)가
비(非)브라우저 User-Agent를 차단하므로, 브라우저 User-Agent 헤더를 넣어 둔 설정입니다.

---
문의/커스터마이즈: `egovframe-sample/pom.xml` 의 의존성과
`src/main/resources/egovframework/` 하위 설정을 참고하세요.
