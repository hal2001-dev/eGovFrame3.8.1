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

## 12. 멀티 데이터소스

DS마다 **DataSource + SqlSessionFactory + 매퍼 스캐너 + 트랜잭션 매니저**를 분리.

| 구분 | 기본(primary) | 두 번째(secondary) |
|------|---------------|--------------------|
| DataSource | `dataSource` (sampledb) | `dataSource2` (sampledb2) |
| SqlSessionFactory | `sqlSession` | `sqlSession2` |
| 매퍼 구분 | `@Mapper` | `@SecondaryMapper` (커스텀 마커) |
| 트랜잭션 매니저 | `txManager` | `txManager2` |

- 두 MapperScannerConfigurer 가 **같은 basePackage** 를 스캔해도 `annotationClass` 로 구분(겹쳐도 안전).
- 두 번째 DS 설정은 `context-datasource-secondary.xml` 한 곳에 모음.
- 데모: `GET /action/multidb/demo.do` → `{db1_sampleCount, db2_productCount, db2_productList}` (한 요청에서 두 DB 조회).
- 실제 운영에선 `dataSource2` url/driver/계정만 바꾸면 다른 실 DB 연결.

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

---
_이 문서는 프로젝트 진행 내역을 정리한 위키입니다. 세부 사용법은 저장소 루트 `README.md` 를 함께 참고하세요._
