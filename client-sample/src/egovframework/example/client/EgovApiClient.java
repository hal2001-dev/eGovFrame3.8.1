package egovframework.example.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * eGovFrame 샘플 REST API 호출용 HMAC 클라이언트 (외부 시스템 참고 구현).
 *
 * <p>서버의 {@code HmacAuthFilter} 와 <b>동일한 규칙</b>으로 요청에 서명합니다.
 * 외부 라이브러리 없이 JDK(HttpURLConnection + javax.crypto)만 사용하므로,
 * JDK 1.8 만 있으면 인터넷/Maven 없이 컴파일·실행할 수 있습니다.</p>
 *
 * <p>서명 대상 문자열(stringToSign) — 각 항목을 개행('\n')으로 연결:</p>
 * <pre>
 *   METHOD
 *   PATH            (요청 경로. 컨텍스트 패스가 있으면 포함, 예: /egovframe-sample/api/samples)
 *   QUERY           (원본 쿼리스트링, 없으면 빈 문자열)
 *   TIMESTAMP       (epoch milliseconds)
 *   SHA-256(body)   (요청 본문의 SHA-256 소문자 16진수, 본문 없으면 빈 문자열의 해시)
 * </pre>
 *
 * <p>실행: {@code java egovframework.example.client.EgovApiClient [baseUrl] [apiKey] [secret]}</p>
 */
public class EgovApiClient {

	/** 요청 헤더 이름(서버와 동일해야 함) */
	private static final String H_KEY = "X-API-KEY";
	private static final String H_TS = "X-API-TIMESTAMP";
	private static final String H_SIG = "X-API-SIGNATURE";

	private final String baseUrl; // 예: http://localhost:8080  (끝에 / 없이)
	private final String apiKey;
	private final String secret;

	public EgovApiClient(String baseUrl, String apiKey, String secret) {
		// 끝의 슬래시 제거(경로 결합 시 // 방지)
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.apiKey = apiKey;
		this.secret = secret;
	}

	/** HTTP 응답(상태코드 + 본문)을 담는 단순 컨테이너 */
	public static class Response {
		public final int status;
		public final String body;

		public Response(int status, String body) {
			this.status = status;
			this.body = body;
		}

		@Override
		public String toString() {
			return "[HTTP " + status + "] " + body;
		}
	}

	/**
	 * 서명된 요청을 전송한다.
	 *
	 * @param method   HTTP 메서드 (GET/POST/PUT/DELETE)
	 * @param pathAndQuery 경로(+쿼리). 예: "/api/samples" 또는 "/api/samples?searchKeyword=HSQL"
	 * @param jsonBody 요청 본문(JSON). 본문이 없으면 null 또는 빈 문자열.
	 * @return 응답 상태코드와 본문
	 */
	public Response send(String method, String pathAndQuery, String jsonBody) throws Exception {
		// 1) 경로와 쿼리 분리 (서명은 경로/쿼리를 분리해 계산)
		String path = pathAndQuery;
		String query = "";
		int q = pathAndQuery.indexOf('?');
		if (q >= 0) {
			path = pathAndQuery.substring(0, q);
			query = pathAndQuery.substring(q + 1);
		}

		byte[] body = (jsonBody == null) ? new byte[0] : jsonBody.getBytes(StandardCharsets.UTF_8);

		// 2) 타임스탬프(epoch ms)와 본문 해시로 서명 대상 문자열 구성
		String timestamp = String.valueOf(System.currentTimeMillis());
		String stringToSign = method + "\n" + path + "\n" + query + "\n"
				+ timestamp + "\n" + sha256Hex(body);
		String signature = hmacSha256Hex(secret, stringToSign);

		// 3) 연결 구성 및 헤더 세팅
		URL url = new URL(baseUrl + pathAndQuery);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod(method);
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);
		con.setRequestProperty(H_KEY, apiKey);
		con.setRequestProperty(H_TS, timestamp);
		con.setRequestProperty(H_SIG, signature);

		// 4) 본문이 있으면 전송(GET/DELETE 는 보통 본문 없음)
		if (body.length > 0) {
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			try (OutputStream os = con.getOutputStream()) {
				os.write(body);
			}
		}

		// 5) 응답 수신(성공/실패 스트림 모두 처리)
		int status = con.getResponseCode();
		InputStream is = (status >= 200 && status < 400) ? con.getInputStream() : con.getErrorStream();
		String respBody = (is == null) ? "" : readAll(is);
		con.disconnect();
		return new Response(status, respBody);
	}

	// ---- 서명 유틸 (서버 HmacUtil 과 동일 알고리즘) ----

	/** SHA-256(data) 를 소문자 16진수로 */
	private static String sha256Hex(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return toHex(md.digest(data));
	}

	/** HMAC-SHA256(secret, message) 를 소문자 16진수로 */
	private static String hmacSha256Hex(String secret, String message) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		return toHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
	}

	private static String toHex(byte[] bytes) {
		char[] hex = "0123456789abcdef".toCharArray();
		char[] out = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			out[i * 2] = hex[v >>> 4];
			out[i * 2 + 1] = hex[v & 0x0F];
		}
		return new String(out);
	}

	private static String readAll(InputStream is) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int n;
		while ((n = is.read(buf)) != -1) {
			bos.write(buf, 0, n);
		}
		return new String(bos.toByteArray(), StandardCharsets.UTF_8);
	}

	// ---- 데모 main: GET → POST → GET → PUT → DELETE → GET(404) 순으로 호출 ----

	public static void main(String[] args) throws Exception {
		String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
		String apiKey = args.length > 1 ? args[1] : "EXTSYS001";
		String secret = args.length > 2 ? args[2] : "change-me-external-system-secret-0001";

		EgovApiClient client = new EgovApiClient(baseUrl, apiKey, secret);
		System.out.println("Target : " + baseUrl + "  (apiKey=" + apiKey + ")");

		System.out.println("\n[1] 목록 조회  GET /api/samples");
		System.out.println("    " + client.send("GET", "/api/samples", null));

		System.out.println("\n[2] 검색      GET /api/samples?searchKeyword=HSQL");
		System.out.println("    " + client.send("GET", "/api/samples?searchKeyword=HSQL", null));

		System.out.println("\n[3] 생성      POST /api/samples");
		String createJson = "{\"name\":\"클라이언트 등록\",\"description\":\"자바 HMAC 클라이언트\",\"useYn\":\"Y\",\"regUser\":\"client\"}";
		Response created = client.send("POST", "/api/samples", createJson);
		System.out.println("    " + created);

		// 생성 응답 JSON 에서 id 추출(간단 파싱; 실제로는 JSON 라이브러리 사용 권장)
		String newId = extractJsonString(created.body, "id");
		System.out.println("    -> 생성된 id = " + newId);

		if (newId != null) {
			System.out.println("\n[4] 단건 조회  GET /api/samples/" + newId);
			System.out.println("    " + client.send("GET", "/api/samples/" + newId, null));

			System.out.println("\n[5] 수정      PUT /api/samples/" + newId);
			String updJson = "{\"name\":\"클라이언트 수정\",\"description\":\"updated\",\"useYn\":\"N\",\"regUser\":\"client\"}";
			System.out.println("    " + client.send("PUT", "/api/samples/" + newId, updJson));

			System.out.println("\n[6] 삭제      DELETE /api/samples/" + newId);
			System.out.println("    " + client.send("DELETE", "/api/samples/" + newId, null));

			System.out.println("\n[7] 삭제 확인  GET /api/samples/" + newId + "  (404 예상)");
			System.out.println("    " + client.send("GET", "/api/samples/" + newId, null));
		}

		System.out.println("\n[8] 잘못된 서명(비밀키 오류) 테스트  (401 예상)");
		EgovApiClient bad = new EgovApiClient(baseUrl, apiKey, "wrong-secret");
		System.out.println("    " + bad.send("GET", "/api/samples", null));
	}

	/** {@code "key":"value"} 패턴에서 문자열 값을 뽑는 아주 단순한 파서(데모용). */
	private static String extractJsonString(String json, String key) {
		if (json == null) {
			return null;
		}
		String needle = "\"" + key + "\":\"";
		int i = json.indexOf(needle);
		if (i < 0) {
			return null;
		}
		int start = i + needle.length();
		int end = json.indexOf('"', start);
		return (end < 0) ? null : json.substring(start, end);
	}
}
