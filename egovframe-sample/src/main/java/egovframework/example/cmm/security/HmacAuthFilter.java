package egovframework.example.cmm.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * REST API(/api/*)를 위한 서버 측 HMAC 인증 필터 (서버-투-서버 연계용).
 *
 * <p>외부 시스템은 아래 3개 헤더를 붙여 요청합니다.</p>
 * <ul>
 *   <li><b>X-API-KEY</b>       : 클라이언트 식별자(DB 에서 비밀키를 조회하는 키)</li>
 *   <li><b>X-API-TIMESTAMP</b> : 요청 시각(epoch milliseconds)</li>
 *   <li><b>X-API-SIGNATURE</b> : HMAC-SHA256(secret, stringToSign) 의 소문자 16진수</li>
 * </ul>
 *
 * <p>서명 대상 문자열(stringToSign) — 각 항목을 개행('\n')으로 연결:</p>
 * <pre>
 *   METHOD          (예: GET, POST)
 *   PATH            (요청 URI, 예: /api/samples/123)
 *   QUERY           (원본 쿼리스트링, 없으면 빈 문자열)
 *   TIMESTAMP       (X-API-TIMESTAMP 와 동일 값)
 *   SHA-256(body)   (요청 본문의 SHA-256 소문자 16진수, 본문 없으면 빈 문자열의 해시)
 * </pre>
 *
 * <p>검증 순서: (1) 전역 IP 화이트리스트 → (2) 필수 헤더 확인 → (3) 클라이언트 조회
 * (사용중지/미등록 거부) → (4) 클라이언트별 IP 화이트리스트 → (5) 타임스탬프 허용오차
 * (재전송 방지) → (6) 서명 재계산·상수시간 비교. 하나라도 실패하면 즉시 거부합니다.</p>
 *
 * <p>API 키·비밀키·허용 IP 는 DB(API_CLIENT / API_CLIENT_IP)에서 관리하며
 * {@link ApiClientAuthService} 가 캐시와 함께 제공합니다. 이 필터는 순수 서블릿 필터라
 * 스프링이 주입해주지 않으므로, 서비스 빈은 요청 시점에 웹 컨텍스트에서 조회합니다.
 * 서명 유효시간·전역 IP·프록시 헤더 같은 배포 설정은 hmac.properties 에서 읽습니다.</p>
 */
public class HmacAuthFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HmacAuthFilter.class);

	/** 요청 헤더 이름 상수 */
	private static final String H_KEY = "X-API-KEY";
	private static final String H_TS = "X-API-TIMESTAMP";
	private static final String H_SIG = "X-API-SIGNATURE";

	/** 전역 허용 IP 대역(CIDR) 목록. 비어 있으면 전역 IP 제한 없음. */
	private List<String> globalWhitelist = java.util.Collections.emptyList();

	/** 실제 클라이언트 IP 를 읽을 헤더 이름(예: X-Forwarded-For, X-Real-IP). 비면 TCP peer 주소 사용. */
	private String forwardedHeader = "";

	/**
	 * 값이 여러 개인 프록시 헤더에서 몇 번째를 실제 클라이언트로 볼지, <b>오른쪽 기준</b> 인덱스.
	 * (0 = 맨 오른쪽) 기본값 -1 은 "맨 왼쪽 값을 클라이언트로 사용"을 의미하며,
	 * 이는 엣지 프록시가 헤더를 정화(덮어쓰기)하는 경우에 올바릅니다.
	 */
	private int forwardedFromRight = -1;

	/** 타임스탬프 허용 오차(밀리초). 기본 5분. 이 범위를 벗어나면 재전송으로 간주해 거부. */
	private long skewMillis = 300_000L;

	/** 서블릿 컨텍스트(스프링 빈 조회에 사용) */
	private ServletContext servletContext;

	/** 지연 조회한 인증 서비스(최초 요청 때 컨텍스트에서 한 번 찾아 보관) */
	private volatile ApiClientAuthService authService;

	/**
	 * 필터 초기화. hmac.properties 에서 배포 설정(허용오차/전역 IP/프록시 헤더)을 읽는다.
	 * (API 키/비밀키/클라이언트별 IP 는 여기서 읽지 않고 DB 에서 관리한다.)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.servletContext = filterConfig.getServletContext();

		String resource = filterConfig.getInitParameter("configLocation");
		if (resource == null || resource.trim().isEmpty()) {
			resource = "hmac.properties";
		}
		Properties props = new Properties();
		try (InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resource)) {
			if (in == null) {
				throw new ServletException("HMAC config not found on classpath: " + resource);
			}
			props.load(in);
		} catch (IOException e) {
			throw new ServletException("Failed to load HMAC config: " + resource, e);
		}

		String skew = props.getProperty("hmac.skewSeconds");
		if (skew != null && !skew.trim().isEmpty()) {
			skewMillis = Long.parseLong(skew.trim()) * 1000L;
		}
		globalWhitelist = CidrUtil.parseList(props.getProperty("ip.whitelist"));
		String fwd = props.getProperty("ip.forwardedHeader");
		forwardedHeader = fwd == null ? "" : fwd.trim();
		String fromRight = props.getProperty("ip.forwardedFromRight");
		if (fromRight != null && !fromRight.trim().isEmpty()) {
			forwardedFromRight = Integer.parseInt(fromRight.trim());
		}
		LOGGER.info("HmacAuthFilter initialized: skew={}ms, globalIpRules={}, forwardedHeader='{}', forwardedFromRight={}",
				skewMillis, globalWhitelist.size(), forwardedHeader, forwardedFromRight);
	}

	/** 요청마다 호출되는 인증 검증 로직. */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// (1) 전역 IP 화이트리스트 — 가장 저렴한 1차 방어선
		String clientIp = resolveClientIp(request);
		if (!globalWhitelist.isEmpty() && !CidrUtil.matchesAny(globalWhitelist, clientIp)) {
			LOGGER.warn("IP {} rejected by global whitelist", clientIp);
			reject(response, 403, "source ip not allowed");
			return;
		}

		// 본문을 캐싱해 서명 검증(여기)과 컨트롤러(@RequestBody)가 모두 읽을 수 있게 한다
		CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);

		String apiKey = request.getHeader(H_KEY);
		String timestamp = request.getHeader(H_TS);
		String signature = request.getHeader(H_SIG);

		// (2) 필수 인증 헤더 확인
		if (isBlank(apiKey) || isBlank(timestamp) || isBlank(signature)) {
			reject(response, 401, "missing authentication headers");
			return;
		}

		// (3) DB(캐시)에서 클라이언트 조회 — 미등록/사용중지 거부
		ApiClientVO client = auth().getClient(apiKey);
		if (client == null) {
			reject(response, 401, "unknown api key");
			return;
		}
		if (!client.isEnabled()) {
			reject(response, 401, "api key disabled");
			return;
		}

		// (4) 클라이언트별 IP 화이트리스트 — 외부 시스템별로 출발지 IP 고정
		List<String> allowed = client.getAllowedCidrs();
		if (allowed != null && !allowed.isEmpty() && !CidrUtil.matchesAny(allowed, clientIp)) {
			LOGGER.warn("IP {} rejected for apiKey={} (per-client whitelist)", clientIp, apiKey);
			reject(response, 403, "source ip not allowed for this api key");
			return;
		}

		// (5) 타임스탬프 허용 오차 확인(재전송 공격 방지)
		long ts;
		try {
			ts = Long.parseLong(timestamp.trim());
		} catch (NumberFormatException e) {
			reject(response, 401, "invalid timestamp");
			return;
		}
		long now = System.currentTimeMillis();
		if (Math.abs(now - ts) > skewMillis) {
			reject(response, 401, "timestamp out of range");
			return;
		}

		// (6) 서명 대상 문자열을 동일 규칙으로 재구성해 서버가 기대 서명을 만들고 비교
		String method = request.getMethod();
		String path = request.getRequestURI();
		String query = request.getQueryString() == null ? "" : request.getQueryString();
		String bodyHash = HmacUtil.sha256Hex(wrapped.getCachedBody());
		String stringToSign = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + bodyHash;
		String expected = HmacUtil.hmacSha256Hex(client.getSecret(), stringToSign);

		if (!HmacUtil.constantTimeEquals(expected, signature.trim().toLowerCase())) {
			LOGGER.warn("HMAC mismatch for apiKey={} method={} path={}", apiKey, method, path);
			reject(response, 401, "signature mismatch");
			return;
		}

		// 인증 통과 — 이후 단계에서 호출자를 식별할 수 있도록 요청 속성에 담아 전달
		request.setAttribute("apiClientId", apiKey);
		chain.doFilter(wrapped, response);
	}

	@Override
	public void destroy() {
		// 정리할 리소스 없음
	}

	/** 스프링 루트 웹 컨텍스트에서 인증 서비스 빈을 지연 조회한다.(최초 1회) */
	private ApiClientAuthService auth() {
		ApiClientAuthService local = authService;
		if (local == null) {
			local = WebApplicationContextUtils
					.getRequiredWebApplicationContext(servletContext)
					.getBean(ApiClientAuthService.class);
			authService = local;
		}
		return local;
	}

	/** 문자열이 null 이거나 공백뿐인지 여부 */
	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	/**
	 * 실제 클라이언트 IP 를 판정한다.
	 *
	 * <p>앞단에 L4 + 웹서버(프록시)가 있으면 {@code request.getRemoteAddr()} 는 프록시 IP 이므로,
	 * ip.forwardedHeader(예: X-Forwarded-For 또는 X-Real-IP)를 지정하고 웹서버가 실제 접속
	 * IP 를 그 헤더에 넣도록 설정해야 합니다. 기본은 헤더의 맨 왼쪽 값을 클라이언트로 보며,
	 * 이는 엣지 프록시가 클라이언트가 보낸 헤더를 덮어쓸 때 안전합니다. 고정된 프록시 체인이라면
	 * ip.forwardedFromRight 로 오른쪽 기준 특정 위치 값을 선택할 수 있습니다.</p>
	 */
	private String resolveClientIp(HttpServletRequest request) {
		if (!forwardedHeader.isEmpty()) {
			String value = request.getHeader(forwardedHeader);
			if (value != null && !value.trim().isEmpty()) {
				String[] parts = value.split(",");
				if (forwardedFromRight >= 0) {
					int idx = parts.length - 1 - forwardedFromRight;
					if (idx >= 0 && idx < parts.length) {
						return parts[idx].trim();
					}
				}
				return parts[0].trim();
			}
		}
		return request.getRemoteAddr();
	}

	/** 인증/인가 실패 시 상태코드와 JSON 메시지로 응답한다.(401 인증 실패 / 403 IP 거부) */
	private static void reject(HttpServletResponse response, int status, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		String body = "{\"status\":" + status + ",\"error\":\""
				+ (status == 403 ? "Forbidden" : "Unauthorized") + "\",\"message\":\""
				+ message + "\"}";
		response.getWriter().write(body);
		response.getWriter().flush();
	}
}
