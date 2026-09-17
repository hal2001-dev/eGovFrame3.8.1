package egovframework.example.cmm.sso;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * SSO 인증 필터 (웹 화면 보호).
 *
 * <p>인증(신원 확인)은 컨트롤러 이전 단계인 <b>필터</b>에서 처리하는 것이 정석입니다
 * (모든 요청 커버, 미인증 시 조기 리다이렉트). 인가(권한 검사)는 핸들러 정보를 쓰는
 * 인터셉터가 적합합니다.</p>
 *
 * <p>동작 순서:</p>
 * <ol>
 *   <li>세션에 인증 사용자({@value #SESSION_USER})가 있으면 통과</li>
 *   <li>없으면 SSO 토큰(파라미터/헤더)을 {@link SsoTokenValidator}로 검증 → 성공 시 세션에 저장하고 통과</li>
 *   <li>둘 다 없으면 SSO 로그인 URL 로 리다이렉트(returnUrl 포함)</li>
 * </ol>
 *
 * <p>이 샘플은 데모 영역 {@code /secure/*} 에 매핑되어 있습니다. 실제로는 웹 앱 경로
 * ({@code /action/*})에 매핑하세요. 로그인 페이지 등 공개 경로는 whitelist(init-param)로 제외합니다.</p>
 */
public class SsoAuthFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SsoAuthFilter.class);

	/** 인증 사용자 세션 키 (컨트롤러와 공유) */
	public static final String SESSION_USER = "ssoUser";

	private String loginUrl = "/sso/login";
	private String tokenParam = "ssoToken";
	private String tokenHeader = "X-SSO-Token";
	private String[] whitelist = new String[0];

	private volatile SsoTokenValidator validator;

	@Override
	protected void initFilterBean() throws ServletException {
		FilterConfig fc = getFilterConfig();
		if (fc != null) {
			loginUrl = orDefault(fc.getInitParameter("loginUrl"), loginUrl);
			tokenParam = orDefault(fc.getInitParameter("tokenParam"), tokenParam);
			tokenHeader = orDefault(fc.getInitParameter("tokenHeader"), tokenHeader);
			String wl = fc.getInitParameter("whitelist");
			if (wl != null && !wl.trim().isEmpty()) {
				whitelist = wl.trim().split("[,\\s]+");
			}
		}
		LOGGER.info("SsoAuthFilter initialized: loginUrl={}, tokenParam={}, tokenHeader={}, whitelist={}",
				loginUrl, tokenParam, tokenHeader, whitelist.length);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		String path = request.getRequestURI().substring(request.getContextPath().length());

		// 공개 경로(로그인 등)는 통과
		for (String w : whitelist) {
			if (path.startsWith(w)) {
				chain.doFilter(request, response);
				return;
			}
		}

		// (1) 이미 인증된 세션이면 통과
		HttpSession session = request.getSession(false);
		if (session != null && session.getAttribute(SESSION_USER) != null) {
			chain.doFilter(request, response);
			return;
		}

		// (2) SSO 토큰(파라미터 또는 헤더) 검증
		String token = request.getParameter(tokenParam);
		if (token == null || token.trim().isEmpty()) {
			token = request.getHeader(tokenHeader);
		}
		SsoUser user = validator().validate(token);
		if (user != null) {
			request.getSession(true).setAttribute(SESSION_USER, user);
			LOGGER.debug("SSO authenticated via token: {}", user.getId());
			chain.doFilter(request, response);
			return;
		}

		// (3) 미인증 → SSO 로그인으로 리다이렉트(returnUrl 포함)
		String returnUrl = request.getRequestURI();
		if (request.getQueryString() != null) {
			returnUrl += "?" + request.getQueryString();
		}
		String target = request.getContextPath() + loginUrl
				+ "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8.name());
		LOGGER.debug("Not authenticated, redirect to SSO login: {}", target);
		response.sendRedirect(target);
	}

	/** 스프링 컨텍스트에서 토큰 검증기 빈을 지연 조회(구현 교체가 쉬움) */
	private SsoTokenValidator validator() {
		SsoTokenValidator local = validator;
		if (local == null) {
			local = WebApplicationContextUtils
					.getRequiredWebApplicationContext(getServletContext())
					.getBean(SsoTokenValidator.class);
			validator = local;
		}
		return local;
	}

	private static String orDefault(String v, String def) {
		return (v == null || v.trim().isEmpty()) ? def : v.trim();
	}
}
