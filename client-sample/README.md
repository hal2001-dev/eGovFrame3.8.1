# HMAC 클라이언트 샘플 (외부 시스템 참고 구현)

eGovFrame 샘플 REST API(`/api/*`)를 **HMAC 서명**으로 호출하는 Java 클라이언트입니다.
서버의 `HmacAuthFilter` 와 동일한 서명 규칙을 구현했으며, **의존성 없이 JDK 1.8 만으로**
컴파일·실행됩니다(오프라인 가능).

## 구성
```
client-sample/
├── src/egovframework/example/client/EgovApiClient.java  # 서명 + 호출 클라이언트
├── run-client.sh    # mac/linux 실행
├── run-client.bat   # Windows 실행
└── README.md
```

## 실행
서버(WAR/Jetty)가 먼저 떠 있어야 합니다. 그다음:

```bash
# mac / linux
export JAVA_HOME=/path/to/jdk1.8
./run-client.sh                                   # 기본값: localhost:8080 / EXTSYS001
./run-client.sh http://localhost:8080 EXTSYS001 change-me-external-system-secret-0001
```
```bat
REM Windows
set "JAVA_HOME=C:\Program Files\Zulu\zulu-8"
run-client.bat http://localhost:8080 EXTSYS001 change-me-external-system-secret-0001
```

인자: `[baseUrl] [apiKey] [secret]` (모두 생략 가능, 생략 시 기본값 사용)

> 톰캣에 컨텍스트 패스로 배포한 경우(예: `/egovframe-sample`)에는
> `baseUrl` 을 `http://호스트:포트/egovframe-sample` 로 주세요.
> (서명 대상 PATH 는 서버가 보는 요청 경로와 정확히 같아야 하며, 코드가 baseUrl+경로를
> 그대로 사용하므로 baseUrl 에 컨텍스트 패스를 포함하면 자동으로 맞습니다.)

## 데모 시나리오
`main()` 이 아래를 순서대로 호출하고 결과(상태코드+본문)를 출력합니다.
1. 목록 조회 `GET /api/samples`
2. 검색 `GET /api/samples?searchKeyword=HSQL`
3. 생성 `POST /api/samples` → 201
4. 단건 조회 `GET /api/samples/{id}`
5. 수정 `PUT /api/samples/{id}`
6. 삭제 `DELETE /api/samples/{id}` → 204
7. 삭제 확인 `GET /api/samples/{id}` → 404
8. 잘못된 비밀키로 호출 → 401

## 다른 언어로 이식할 때 (서명 규칙 요약)
```
stringToSign = METHOD + "\n" + PATH + "\n" + QUERY + "\n" + TIMESTAMP + "\n" + hex(SHA-256(body))
signature    = hex( HMAC-SHA256(secret, stringToSign) )   // 소문자
headers      : X-API-KEY, X-API-TIMESTAMP(epoch ms), X-API-SIGNATURE
```
- 본문이 없으면 `SHA-256("")` 사용.
- TIMESTAMP 는 서버 시각과 ±300초 이내여야 함(재전송 방지).
- 반드시 HTTPS(TLS) 위에서 사용 권장(HMAC 은 무결성·재전송 방지용, 본문 암호화 아님).
