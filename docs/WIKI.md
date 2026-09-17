# eGovFrame 3.8 Sample — 프로젝트 위키

전자정부 표준프레임워크 **3.8**(RTE 3.8.0) 기반의 웹 + REST 샘플 프로젝트 정리 문서입니다.
JSP 화면 CRUD, JSON REST API, HMAC 서버-투-서버 인증, IP 화이트리스트, DB 기반 클라이언트 관리,
멀티 데이터소스, 오프라인 빌드(윈도우 포함)까지 다룹니다.

- 저장소: <https://github.com/hal2001-dev/eGovFrame3.8.1>
- 릴리스(배포용 WAR): <https://github.com/hal2001-dev/eGovFrame3.8.1/releases>

## 목차
1. [개요 / 기술 스택](#1-개요--기술-스택)
2. [프로젝트 구조](#2-프로젝트-구조)
3. [빌드 & 실행 (오프라인)](#3-빌드--실행-오프라인)
4. [데이터베이스 (HSQLDB / Oracle)](#4-데이터베이스-hsqldb--oracle)
5. [REST API 명세](#5-rest-api-명세)
6. [인증 — HMAC 서명](#6-인증--hmac-서명)
7. [IP 화이트리스트 & L4/프록시](#7-ip-화이트리스트--l4프록시)
8. [API 클라이언트 DB 관리](#8-api-클라이언트-db-관리)
9. [클라이언트 샘플](#9-클라이언트-샘플)
10. [필터 분리 (웹/REST)](#10-필터-분리-웹rest)
11. [DispatcherServlet & 컨텍스트 계층](#11-dispatcherservlet--컨텍스트-계층)
12. [멀티 데이터소스](#12-멀티-데이터소스)
13. [트랜잭션 분리 방식](#13-트랜잭션-분리-방식)
14. [배포 (WAR / 톰캣)](#14-배포-war--톰캣)
15. [형상관리 (Git / SVN) & 릴리스](#15-형상관리-git--svn--릴리스)
16. [업무별 폴더 구조 가이드](#16-업무별-폴더-구조-가이드)
17. [검증 결과 요약](#17-검증-결과-요약)
18. [설계 논의 & FAQ (계속 업데이트)](#18-설계-논의--faq-계속-업데이트)
19. [SSO 인증 필터 (샘플)](#19-sso-인증-필터-샘플)

---

## 1. 개요 / 기술 스택

| 구분 | 버전 |
|------|------|
| eGovFrame RTE | 3.8.0 |
| Spring Framework | 4.3.16.RELEASE |
| MyBatis / mybatis-spring | 3.4.1 / 1.3.0 |
| REST(JSON) | Jackson 2.9.10 |
| Logging | Log4j2 2.12.4 (SLF4J 경유) |
| Servlet / JSP | 3.1 / 2.3 (JSTL 1.2) |
| DB(기본/데모) | HSQLDB 2.3.2 (in-memory) |
| DB(운영 옵션) | Oracle (ojdbc8 21.9.0.0), `spring.profiles.active=oracle` |
| JDK | 1.8 |
| 빌드 | Maven (저장소에 3.9.16 번들) |

기능: 게시판(Sample) 목록/검색/등록/수정/삭제 — **JSP 화면 + REST API** 동시 제공.
DB는 기본 HSQLDB 인메모리라 외부 DB 없이 바로 구동됩니다.

## 2. 프로젝트 구조

```
egovframework/example
├── cmm/                         # 공통(cross-cutting)
│   ├── annotation/SecondaryMapper.java   # 두 번째 DS 매퍼 마커
│   ├── security/                # HMAC 인증(필터/유틸/DB 클라이언트)
│   │   ├── HmacAuthFilter, HmacUtil, CidrUtil, CachedBodyHttpServletRequest
│   │   └── ApiClientVO, ApiClientMapper, ApiClientAuthService
│   └── web/WebCommonFilter.java # 웹(화면) 전용 공통 필터
├── sample/                      # 게시판 업무 (기본 DB)
│   ├── web/     EgovSampleController(화면), EgovSampleApiController(REST), EgovMultiDbController(데모)
│   └── service/ (+impl/)  SampleVO, SampleDefaultVO, EgovSampleService(+Impl), SampleMapper
└── secondary/                   # 두 번째 DB 업무 (상품)
    └── service/ (+impl/)  ProductVO, SecondaryService(+Impl), ProductMapper(@SecondaryMapper)

src/main/resources/egovframework/
├── spring/  context-common / context-datasource / context-datasource-secondary
│            context-mapper / context-transaction / springmvc/dispatcher-servlet
├── mapper/example/*.xml (기본 DB), mapper/secondary/*.xml (두 번째 DB)
└── message/ , log4j2.xml , hmac.properties , oracle.properties
src/main/resources/db/  schema-hsql, data-hsql, schema2-hsql, data2-hsql, oracle/*.sql
src/main/webapp/WEB-INF/  web.xml , jsp/sample/*.jsp
```

## 3. 빌드 & 실행 (오프라인)

**필요한 것은 JDK 1.8 하나뿐** (Maven·의존 JAR은 저장소에 번들: `tools/apache-maven-3.9.16`, `m2-repo`).

```bash
# mac / linux
export JAVA_HOME=/path/to/jdk1.8
./build-offline.sh      # → egovframe-sample/target/egovframe-sample.war
./run-offline.sh        # → http://localhost:8080/
```
```bat
REM Windows
set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"
build-offline.bat
run-offline.bat
```
- 내부적으로 `mvn -o -Dmaven.repo.local=./m2-repo ...` (완전 오프라인).
- 루트 접속 시 `/action/sample/egovSampleList.do` 목록으로 이동.

## 4. 데이터베이스 (HSQLDB / Oracle)

Spring 프로파일로 전환합니다. 매퍼 SQL은 공용(`LIKE ... || ...`, 표준 CRUD).

- **기본 = HSQLDB 인메모리**: 기동 시 스키마+시드 자동 적재(`db/schema-hsql.sql`, `db/data-hsql.sql`).
- **Oracle**: `-Dspring.profiles.active=oracle`
  1. `db/oracle/schema-oracle.sql`(+`data-oracle.sql`)로 테이블 생성
  2. `oracle.properties` 접속정보 수정
  3. oracle 프로파일로 기동 (ojdbc8 번들 포함)
  - oracle 프로파일은 DDL 자동 실행 안 함(운영 DB 보호). DB 미접속 시 인증은 fail-closed(401).

## 5. REST API 명세

베이스: `/api/samples` (JSON). 모든 `/api/*` 는 HMAC 인증 필요.

| Method | URL | 설명 | 응답 |
|--------|-----|------|------|
| GET | `/api/samples` (`?searchCondition=&searchKeyword=`) | 목록/검색 | `{totalCount, list}` |
| GET | `/api/samples/{id}` | 단건 | `SampleVO` / 없으면 404 |
| POST | `/api/samples` | 생성 | 201 + 리소스 + Location |
| PUT | `/api/samples/{id}` | 수정 | 수정 리소스 / 404 |
| DELETE | `/api/samples/{id}` | 삭제 | 204 / 404 |

- 응답 JSON은 `@JsonIgnoreProperties` 로 페이징 필드 제외(id/name/description/useYn/regUser).
- null 필드까지 빼려면 `@JsonInclude(Include.NON_NULL)` 추가(구버전 `@JsonSerialize(include=)`는 사용 금지).

## 6. 인증 — HMAC 서명

서버-투-서버(M2M). 로그인/세션이 아니라 **API Key + Secret 으로 매 요청 서명**.

**헤더 3개**

| 헤더 | 값 |
|------|-----|
| `X-API-KEY` | 클라이언트 식별자 |
| `X-API-TIMESTAMP` | 요청 시각(epoch ms) |
| `X-API-SIGNATURE` | `HMAC-SHA256(secret, stringToSign)` 소문자 hex |

**서명 대상(stringToSign)** — `\n` 연결:
```
METHOD
PATH             (요청 경로, 컨텍스트 패스 포함)
QUERY            (원본 쿼리스트링, 없으면 "")
TIMESTAMP
SHA-256(body)    (소문자 hex, 본문 없으면 "" 의 해시)
```
**검증 규칙**: 헤더 누락/미등록 키/사용중지/서명 불일치 → 401, 타임스탬프 ±300초 초과 → 401(재전송 방지), 상수시간 비교.

> HMAC은 무결성·재전송 방지용이며 본문 암호화가 아님 → 실제 연계는 **HTTPS(TLS)** 위에서.

## 7. IP 화이트리스트 & L4/프록시

CIDR 기반(C클래스=`/24`, B=`/16`, 단일 IP=`/32`). IP 거부는 401과 구분해 **403**.

- **전역**: `hmac.properties` 의 `ip.whitelist` (비우면 제한 없음)
- **클라이언트별**: DB `API_CLIENT_IP` 테이블 (그 키에 행이 있으면 그 대역만 허용)
- 검사 순서: 전역 → HMAC 서명 → 클라이언트별

**L4 + 웹서버(프록시) 뒤**: `getRemoteAddr()` 는 프록시 IP 이므로, 웹서버가 실제 IP를 헤더로 넣게 하고 앱이 읽음.
```properties
ip.forwardedHeader=X-Real-IP       # 권장(웹서버가 넣는 단일 값)
#ip.forwardedFromRight=            # XFF 체인일 때 오른쪽 기준 인덱스
```
- nginx: `proxy_set_header X-Real-IP $remote_addr;`
- Apache: `RequestHeader set X-Real-IP "%{REMOTE_ADDR}s"` (또는 mod_remoteip)
- ⚠️ 엣지가 클라이언트가 보낸 헤더를 **덮어써야** 위조 방지. L4가 SNAT면 클라이언트 IP 보존 여부 확인 필요.

## 8. API 클라이언트 DB 관리

키/시크릿/허용 IP를 DB에서 관리 → 재배포 없이 발급·폐기·IP 변경(캐시 TTL 60초 내 반영).

| 테이블 | 컬럼 | 설명 |
|--------|------|------|
| `API_CLIENT` | `API_KEY`(PK), `SECRET`, `USE_YN`, `CLIENT_NAME` | `USE_YN='N'` 이면 즉시 차단 |
| `API_CLIENT_IP` | `API_KEY`(FK), `CIDR` | 없으면 IP 제한 없음 |

```sql
INSERT INTO API_CLIENT (API_KEY, SECRET, USE_YN, CLIENT_NAME) VALUES ('EXTSYS002','a-long-secret','Y','External 2');
INSERT INTO API_CLIENT_IP (API_KEY, CIDR) VALUES ('EXTSYS002','203.0.113.0/24');
UPDATE API_CLIENT SET USE_YN='N' WHERE API_KEY='EXTSYS002';   -- 폐기
```

## 9. 클라이언트 샘플

`client-sample/EgovApiClient.java` — 서버와 동일한 HMAC 규칙으로 호출하는 **의존성 없는 Java 클라이언트**(JDK만).
```bash
export JAVA_HOME=/path/to/jdk1.8
./client-sample/run-client.sh   # [baseUrl] [apiKey] [secret]
```
데모: 목록→검색→생성(201)→조회→수정→삭제(204)→404→잘못된서명(401) 순 호출.
다른 언어로 이식 시 서명 규칙은 6장 참고.

## 10. 필터 분리 (웹/REST)

URL 접두사로 필터를 분리(방법 A). DispatcherServlet은 1개 유지.

| 필터 | 매핑 | 역할 |
|------|------|------|
| `CharacterEncodingFilter` | `/*` | 공통(UTF-8) |
| `WebCommonFilter` | `/action/*` | 웹 전용(화면 공통 처리) |
| `HmacAuthFilter` | `/api/*` | REST 전용(HMAC 인증) |

- 웹 URL은 `/action/sample/*`, REST는 `/api/*` 로 접두사가 겹치지 않아 exclude 로직 불필요.
- 필터 실행 순서 = `<filter-mapping>` 선언 순서.

## 11. DispatcherServlet & 컨텍스트 계층

- **Root 컨텍스트(부모, 1개)**: `ContextLoaderListener` + `context-*.xml` → Service/Mapper/DataSource/Tx 등 공유 빈.
- **Servlet 컨텍스트(자식)**: DispatcherServlet 마다 `dispatcher-servlet.xml` → Controller/뷰리졸버/컨버터.
- 가시성: 자식→부모 보임, 부모→자식·형제↔형제 안 보임.
- 현재는 **DispatcherServlet 1개**(`/` 매핑 + `<mvc:default-servlet-handler/>`). `/` 는 `/*` 와 달라 JSP/정적 처리에 안전(권장).
- 웹/REST의 MVC 설정 자체를 크게 다르게 가져가려면 DispatcherServlet 2개(자식 컨텍스트 2개)로 승격 가능.

## 12. 멀티 데이터소스 (방법론)

### 12.1 원칙 — DS마다 4가지를 분리
데이터소스를 2개 이상 쓸 때는 **DS 하나당 아래 4가지 빈을 각각** 둡니다. 이 세트를 DS 수만큼 복제하는 것이 핵심입니다.

1. **DataSource** — 커넥션 풀/접속정보
2. **SqlSessionFactory** — 그 DataSource + 매퍼 XML 위치
3. **매퍼 스캐너**(MapperScannerConfigurer) — 어느 매퍼를 어느 Factory에 바인딩할지
4. **트랜잭션 매니저**(DataSourceTransactionManager) — 그 DataSource 전용

| 구분 | 기본(primary) | 두 번째(secondary) |
|------|---------------|--------------------|
| DataSource | `dataSource` (sampledb) | `dataSource2` (sampledb2) |
| SqlSessionFactory | `sqlSession` | `sqlSession2` |
| 매퍼 구분 | `@Mapper` | `@SecondaryMapper` (커스텀 마커) |
| 서비스 트랜잭션 | `@Transactional` | `@Transactional("txManager2")` |
| 트랜잭션 매니저 | `txManager` | `txManager2` |

### 12.2 구성 단계
1. `context-datasource-secondary.xml` **한 파일**에 두 번째 DS 일체(DataSource2 + SqlSessionFactory2 + 매퍼 스캐너2 + txManager2)를 모은다.
2. `mapperLocations` 를 DS별로 분리(예: `mapper/example/*.xml` vs `mapper/secondary/*.xml`).
3. 트랜잭션은 `<tx:annotation-driven>` 하나 두고 서비스에서 매니저를 애노테이션으로 지정([13장](#13-트랜잭션-분리-방식)).
4. 데모 `GET /action/multidb/demo.do` → 한 요청에서 두 DB 조회(`{db1_sampleCount, db2_productCount, db2_productList}`).
5. 운영에선 `dataSource2` 의 url/driver/계정만 실제 DB로 교체.

### 12.3 매퍼/서비스를 어느 DS에 붙일지 — 2가지 방법론
매퍼 스캐너와 트랜잭션이 "어느 빈을 어느 DS에 매핑할지" 결정하는데, 그 기준으로 **패키지** 또는 **애노테이션** 을 씁니다.

| | 방법 ① 패키지 분리 | 방법 ② 애노테이션 분리 (현재 채택) |
|---|---|---|
| 매퍼 구분 | 스캐너 `basePackage` 를 서로 다르게 (예: `...sample,...cmm` vs `...secondary`) | 같은 `basePackage` + `annotationClass` (`@Mapper` vs `@SecondaryMapper`) |
| 서비스 트랜잭션 | 패키지 기반 AOP 포인트컷 (`execution(* ...secondary..impl.*Impl.*(..))`) | `@Transactional("txManager2")` (또는 AOP `@within(@마커)`) |
| 매퍼 배치 제약 | DS별 **패키지를 나눠야** 함 | **같은 패키지에 섞여도 무방** |
| 장점 | 설정만으로 명확, 코드 손 안 댐 | 패키지 구조에 자유, 업무별 폴더와 잘 맞음 |
| 단점 | 스캐너 패키지가 겹치면 바인딩 충돌 | 매퍼/서비스마다 애노테이션 정확히 부착 |
| 관례 | 전통적 eGov 프로젝트 | 최근 스타일 |

- ⚠️ **방법 ①에서는 두 스캐너의 `basePackage` 가 겹치면 안 됩니다**(겹치면 매퍼가 어느 Factory에 붙을지 충돌). `basePackage` 는 콤마/세미콜론으로 여러 개 지정 가능.
- ✅ **방법 ②는 같은 `basePackage(egovframework.example)` 를 두 스캐너가 스캔해도** `@Mapper` / `@SecondaryMapper` 로 갈려 안전 — 그래서 현재 이 방식을 씁니다.
- 두 방법은 **혼용 가능**하지만(매퍼는 애노테이션, 서비스는 패키지 등), 팀 내 일관성 유지를 권장합니다.

### 12.4 주의 — 트랜잭션 경계
`txManager` 와 `txManager2` 는 **서로 독립된 트랜잭션**입니다. 한 서비스 메서드에서 두 DB를 **하나의 원자적 커밋/롤백**으로 묶으려면 일반 DataSourceTransactionManager로는 불가하고 **JTA/XA(분산 트랜잭션, 예: Atomikos)** 가 필요합니다.

## 13. 트랜잭션 분리 방식

서비스도 애노테이션으로 DS별 트랜잭션 라우팅(현재 방식 A).

- **A. `@Transactional` (현재)**: `<tx:annotation-driven transaction-manager="txManager"/>` 하나 선언.
  - 기본 DB 서비스: `@Transactional` (default = txManager)
  - 두 번째 DB 서비스: `@Transactional("txManager2")`
- **B. AOP 포인트컷 + 마커**: `@within(@SecondaryTx)` 로 어드바이스 분리(전통적 eGov 스타일). A/B는 둘 중 하나만.
- ⚠️ 두 트랜잭션 매니저는 **독립 트랜잭션**. 한 트랜잭션으로 두 DB 묶으려면 JTA/XA 필요.

## 14. 배포 (WAR / 톰캣)

`build-offline` 산출물 `egovframe-sample.war` 를 톰캣(8.5/9, JDK8) `webapps/` 에 배포.
- 목록: `http://host:8080/egovframe-sample/action/sample/egovSampleList.do`
- REST: `http://host:8080/egovframe-sample/api/samples` (HMAC 서명 필요)
- ROOT 배포: `ROOT.war` 로 이름 변경.
- Oracle: 톰캣 JVM 옵션 `-Dspring.profiles.active=oracle`.

## 15. 형상관리 (Git / SVN) & 릴리스

- **Git(GitHub)**: SSH push (`git@github.com:hal2001-dev/eGovFrame3.8.1.git`). 소스 + `m2-repo`(오프라인 JAR) 커밋, `*.zip`/`*.war`/`target/` 은 제외. 배포 WAR/zip은 Releases 자산으로.
- **SVN + 이클립스 공유**:
  - (A) `m2-repo.zip` 을 각자 `~/.m2/repository` 에 병합 후 Eclipse Offline 체크(가장 간단)
  - (B) `m2-repo` + `settings.xml`(`<localRepository>`) 를 SVN 공유, Eclipse User Settings 지정
  - (C) Maven 없이 `WEB-INF/lib` 참조하는 Dynamic Web Project 버전 사용

## 16. 업무별 폴더 구조 가이드

**업무(도메인) 우선** 구조 권장: 최상위를 업무로 나누고 그 안에서 계층(web/service/impl).
```
egovframework/example/
├── cmm/          # 공통(최소화)
├── sample/       # 업무1  (web / service / service.impl)
├── member/       # 업무2  ...폴더만 추가하면 확장
└── order/        # 업무3
```
- 매퍼 XML·JSP 경로도 업무명으로 미러링, 트랜잭션 AOP·매퍼 스캔은 범용 규칙이라 업무 추가 시 설정 변경 최소.
- 공통은 `cmm` 에 몰되 최소화, 업무 전용 유틸/VO는 각 업무 폴더에.
- 업무 간에는 상대 업무의 **service 인터페이스**에만 의존(구현/매퍼 직접 참조 금지).

## 17. 검증 결과 요약

로컬(내장 Jetty)에서 실제 호출로 확인한 항목:

| 항목 | 결과 |
|------|------|
| 오프라인 빌드(mac, 번들 Maven) | BUILD SUCCESS, WAR 생성 |
| 웹 CRUD(JSP) | 목록/등록/수정/삭제 200, 한글 정상 |
| REST CRUD | 200/201/204, 없는 ID 404 |
| HMAC 인증 | 정상 200, 무헤더/미등록/서명불일치/만료 401 |
| IP 화이트리스트(CIDR) | /24 경계 포함 허용/거부 정확, 403 |
| DB 기반 클라이언트 | 발급/사용중지(401)/클라이언트별 IP 동작 |
| L4/프록시 IP 추출(XFF) | leftmost 클라이언트 판정 확인 |
| 필터 분리 | 웹=WebCommonFilter, /api=HMAC 만 적용 |
| 멀티 DS | 한 요청에서 두 DB 조회, 쓰기 커밋(count 3→4) |
| 클라이언트 샘플 | 서버 호출 전 시나리오 통과 |
| SSO 인증 필터 | 미인증 302, 토큰/세션 200, 로그아웃 302, /action·/api 회귀 정상 |

## 18. 설계 논의 & FAQ (계속 업데이트)

> 프로젝트를 진행하며 나온 **질문·설계 결정**을 이 섹션에 계속 누적합니다.
> (대화에서 새로운 논의가 나오면 여기에 항목을 추가합니다.)

**Q. REST 응답에서 null 필드를 빼려면 어떤 애노테이션?**
→ `@JsonInclude(JsonInclude.Include.NON_NULL)` (jackson-annotations). 클래스/필드/전역(ObjectMapper) 적용 가능.
구버전 `@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)` 은 `Inclusion` enum 이 제거되어 **사용 금지**. 페이징 필드 제거는 `@JsonIgnoreProperties`. → [5장](#5-rest-api-명세)

**Q. 웹용 필터와 REST 전용 필터를 분리하려면?**
→ URL 접두사로 분리(방법 A, 현재): 공통 `/*`, 웹 `WebCommonFilter` `/action/*`, REST `HmacAuthFilter` `/api/*`.
서블릿까지 나누는 방법 B는 `<filter-mapping>` 을 `<servlet-name>` 으로 매핑. → [10장](#10-필터-분리-웹rest)

**Q. `webCommonFilter` 를 `/action/*` 로 두면 DispatcherServlet도 2개가 되나?**
→ 아니오. 필터만 분리하고 DispatcherServlet은 1개(`/` 매핑) 유지. 컨트롤러가 `@RequestMapping` 경로로 구분. → [11장](#11-dispatcherservlet--컨텍스트-계층)

**Q. `servlet-mapping` 이 `/` 인데 문제없나?**
→ 정상·권장. `/`(디폴트 서블릿)는 `/*`와 달라 `*.jsp`는 컨테이너 JspServlet이 처리. 정적리소스는 `<mvc:default-servlet-handler/>` 가 컨테이너 기본 서블릿으로 위임. → [11장](#11-dispatcherservlet--컨텍스트-계층)

**Q. "컨텍스트가 분리된다"는 의미?**
→ Root(부모, 공유 Service/DAO/DataSource) 1개 + DispatcherServlet마다 자식(Controller/뷰) 컨텍스트. 서블릿 2개면 자식 2개(부모는 공유). 자식→부모 가시, 형제↔형제 불가. → [11장](#11-dispatcherservlet--컨텍스트-계층)

**Q. MapperScannerConfigurer `basePackage` 를 여러 개 둘 수 있나?**
→ 예. 콤마/세미콜론/공백 구분으로 여러 패키지 지정. 단 **두 DS 스캐너의 패키지가 겹치면 안 됨**(겹치면 매퍼 바인딩 충돌). → [12장](#12-멀티-데이터소스)

**Q. 패키지 대신 애노테이션으로 두 번째 DS 매퍼를 구분?**
→ 커스텀 마커 `@SecondaryMapper` + 스캐너 `annotationClass` 지정. 두 스캐너가 같은 basePackage 를 스캔해도 애노테이션으로 갈림. → [12장](#12-멀티-데이터소스)

**Q. 서비스도 DS별로 분리해야 하나? 트랜잭션은?**
→ 예. `<tx:annotation-driven>` + `@Transactional("txManager2")` 로 서비스도 애노테이션 기반 라우팅(패키지 독립). 두 트랜잭션은 독립(원자적 2-DB 커밋은 JTA/XA 필요). → [13장](#13-트랜잭션-분리-방식)

**Q. 서비스 포인트컷에 애노테이션을 추가해 분리할 수도 있나?**
→ 가능. AOP 지시자 `@within(@마커)`(클래스) / `@annotation(@마커)`(메서드)로 어드바이스를 나눔. `tx:annotation-driven` 방식과는 둘 중 하나만 사용. → [13장](#13-트랜잭션-분리-방식)

**Q. SSO 인증 처리는 Filter vs Interceptor 어디에?**
→ **인증(신원 확인)은 Filter**(모든 요청 커버, 컨트롤러 이전 차단·IdP 리다이렉트, SSO 제품/Spring Security가 필터). **인가(권한·메뉴)는 Interceptor**(핸들러 정보 활용). 규모가 크면 Spring Security 권장. 구현 샘플 → [19장](#19-sso-인증-필터-샘플)

**Q. 업무별 폴더 구조는?**
→ 업무(도메인) 우선(package-by-feature): 최상위를 업무로 나누고 그 안에 web/service/impl. 공통은 `cmm` 최소화. → [16장](#16-업무별-폴더-구조-가이드)

**Q. `BouncyCastleProvider` 오류가 난다 (기존 소스가 BC 사용)**
→ 기본 샘플은 순수 JDK `javax.crypto`만 써서 BC가 없음. 기존/추가 소스가 BC를 쓰면 **bcprov jar**를 넣어야 함.
JDK 1.8은 `org.bouncycastle:bcprov-jdk15on:1.70` 추가(오프라인이면 온라인에서 한 번 받아 `m2-repo`에 반영 후 재빌드). 오류별:
`NoClassDefFoundError …BouncyCastleProvider`=jar 없음(의존성 추가), `NoSuchProviderException: no such provider: BC`=`Security.addProvider(new BouncyCastleProvider())` 등록 필요, `JCE cannot authenticate the provider BC`=jar 버전/중복 문제(JDK8엔 jdk15on 1.70, 중복 제거). PKIX/CMS/S-MIME까지 쓰면 `bcpkix-jdk15on` 도 추가.

**Q. 오프라인에서 Maven이 m2-repo를 참조하게 하려면? SVN 공유는?**
→ `mvn -o -Dmaven.repo.local=…/m2-repo`, 또는 `settings.xml`의 `<localRepository>`+offline, 또는 `~/.m2/repository`에 병합. Maven을 안 쓰면 `WEB-INF/lib` 참조 Dynamic Web Project. → [15장](#15-형상관리-git--svn--릴리스)

## 19. SSO 인증 필터 (샘플)

인증(신원 확인)은 **필터**에서 처리하는 정석 예제입니다. 기존 웹(`/action/*`)·REST(`/api/*`)를
건드리지 않도록 **데모 보호 영역 `/secure/*`** 에 적용했습니다(실무에선 `/action/*` 에 매핑).

### 구성 (`egovframework.example.cmm.sso`)
| 파일 | 역할 |
|------|------|
| `SsoAuthFilter` | 세션 확인 → SSO 토큰 검증 → 미인증 시 로그인 리다이렉트 (OncePerRequestFilter) |
| `SsoTokenValidator` (인터페이스) | 토큰 검증 확장 지점(SPI) |
| `DemoSsoTokenValidator` (@Service) | 데모 구현 — `demo-token-<id>` 를 유효 토큰으로 처리. **실무에선 교체** |
| `SsoUser` | 인증 사용자(세션 저장) |
| `web/SsoLoginController` | `/sso/login`(GET/POST), `/sso/logout` — 데모 로그인 |
| `web/SecureController` | `/secure/home.do` — 보호 영역 진입 확인(JSON) |

`web.xml`: `ssoAuthFilter` 를 `/secure/*` 에 매핑(로그인 `/sso/*` 는 매핑 밖이라 공개).

### 동작
1. 세션에 인증 사용자(`ssoUser`)가 있으면 통과
2. 없으면 SSO 토큰(파라미터 `ssoToken` 또는 헤더 `X-SSO-Token`)을 검증 → 성공 시 세션 저장 후 통과
3. 둘 다 없으면 `/sso/login?returnUrl=…` 로 리다이렉트

```bash
# 미인증 → 로그인 리다이렉트
curl -i http://localhost:8080/secure/home.do            # 302 → /sso/login?returnUrl=...
# 토큰으로 접근(세션 생성)
curl -c cj http://localhost:8080/secure/home.do?ssoToken=demo-token-alice   # 200 {userId:"alice", ...}
# 세션 쿠키로 재접근
curl -b cj http://localhost:8080/secure/home.do          # 200
# 로그아웃
curl -b cj http://localhost:8080/sso/logout              # 302 → /sso/login
```

### 실무 적용 시
- `DemoSsoTokenValidator` 를 실제 SSO 검증(SAML Assertion, OAuth2/OIDC introspection, 기관 SSO 에이전트 API)으로 교체.
- 로그인 화면 대신 **IdP 리다이렉트 + 콜백 토큰 검증** 흐름으로 변경.
- 필터 매핑을 실제 웹 경로(`/action/*`)로. 인증 이후의 **권한(인가)** 은 `HandlerInterceptor` 로 분리([FAQ](#18-설계-논의--faq-계속-업데이트)).
- 규모가 크면 **Spring Security** 로 통합(SSO/SAML/OAuth2, URL·메서드 권한, CSRF).

---
_이 문서는 프로젝트 진행 내역과 설계 논의를 정리한 위키입니다. 세부 사용법은 저장소 루트 `README.md` 를 함께 참고하세요. (18장은 대화 진행에 따라 계속 갱신)_
