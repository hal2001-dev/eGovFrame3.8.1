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
 * Server-side HMAC authentication for the REST API (server-to-server).
 *
 * <p>The calling system must send three headers:</p>
 * <ul>
 *   <li><b>X-API-KEY</b>       : the client id (looked up in the DB to a shared secret)</li>
 *   <li><b>X-API-TIMESTAMP</b> : request time as epoch milliseconds</li>
 *   <li><b>X-API-SIGNATURE</b> : lowercase hex HMAC-SHA256(secret, stringToSign)</li>
 * </ul>
 *
 * <p>stringToSign (each part separated by '\n'):</p>
 * <pre>
 *   METHOD
 *   PATH            (request URI, e.g. /api/samples/123)
 *   QUERY           (raw query string, or empty)
 *   TIMESTAMP
 *   SHA-256(body) as lowercase hex   (empty body -> hash of "")
 * </pre>
 *
 * <p>API keys / secrets / allowed IPs are managed in the DB (API_CLIENT,
 * API_CLIENT_IP) via {@link ApiClientAuthService}. Deployment-level knobs
 * (clock skew, global IP whitelist, proxy header) come from a properties file.</p>
 */
public class HmacAuthFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(HmacAuthFilter.class);

	private static final String H_KEY = "X-API-KEY";
	private static final String H_TS = "X-API-TIMESTAMP";
	private static final String H_SIG = "X-API-SIGNATURE";

	/** global source IP allow-list (CIDR); empty -> allow all source IPs */
	private List<String> globalWhitelist = java.util.Collections.emptyList();
	/** if set (e.g. X-Forwarded-For / X-Real-IP), read the client IP from this header */
	private String forwardedHeader = "";
	/**
	 * which token of a multi-value forwarded header is the real client, counted
	 * from the RIGHT (0 = rightmost). -1 (default) = use the leftmost token, which
	 * is correct when the edge proxy sanitizes the header.
	 */
	private int forwardedFromRight = -1;
	private long skewMillis = 300_000L; // default 5 minutes

	private ServletContext servletContext;
	private volatile ApiClientAuthService authService;

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

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		// (1) global source-IP allow-list (cheap first line of defense)
		String clientIp = resolveClientIp(request);
		if (!globalWhitelist.isEmpty() && !CidrUtil.matchesAny(globalWhitelist, clientIp)) {
			LOGGER.warn("IP {} rejected by global whitelist", clientIp);
			reject(response, 403, "source ip not allowed");
			return;
		}

		// cache body so both signature check and controller can read it
		CachedBodyHttpServletRequest wrapped = new CachedBodyHttpServletRequest(request);

		String apiKey = request.getHeader(H_KEY);
		String timestamp = request.getHeader(H_TS);
		String signature = request.getHeader(H_SIG);

		if (isBlank(apiKey) || isBlank(timestamp) || isBlank(signature)) {
			reject(response, 401, "missing authentication headers");
			return;
		}

		// look up the client (credentials + allowed IPs) from the DB (cached)
		ApiClientVO client = auth().getClient(apiKey);
		if (client == null) {
			reject(response, 401, "unknown api key");
			return;
		}
		if (!client.isEnabled()) {
			reject(response, 401, "api key disabled");
			return;
		}

		// (2) per-API-key source-IP allow-list (pin each external system to its IPs)
		List<String> allowed = client.getAllowedCidrs();
		if (allowed != null && !allowed.isEmpty() && !CidrUtil.matchesAny(allowed, clientIp)) {
			LOGGER.warn("IP {} rejected for apiKey={} (per-client whitelist)", clientIp, apiKey);
			reject(response, 403, "source ip not allowed for this api key");
			return;
		}

		// replay window
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

		// rebuild stringToSign and compare
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

		// authenticated; expose the caller id downstream
		request.setAttribute("apiClientId", apiKey);
		chain.doFilter(wrapped, response);
	}

	@Override
	public void destroy() {
		// nothing
	}

	/** lazily obtain the Spring-managed service from the root web application context */
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

	private static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	/**
	 * Client IP. Behind an L4 + web server (proxy), request.getRemoteAddr() is the
	 * proxy address, so set ip.forwardedHeader (e.g. X-Forwarded-For or X-Real-IP)
	 * and have the web server populate it from the real connection. By default the
	 * leftmost value is treated as the client (correct when the edge sanitizes the
	 * header); set ip.forwardedFromRight to pick a specific token counted from the
	 * right for fixed proxy chains.
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
