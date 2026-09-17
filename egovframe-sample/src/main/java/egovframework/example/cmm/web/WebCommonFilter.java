package egovframework.example.cmm.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 웹(화면) 전용 공통 필터.
 *
 * <p>web.xml 에서 <b>{@code /action/*}</b> 에만 매핑되므로 화면(액션) 요청에만 동작하고,
 * REST({@code /api/*}) 요청에는 관여하지 않습니다. 경로 접두사로 이미 분리되어 있어
 * 별도의 제외(exclude) 로직은 필요하지 않습니다.</p>
 *
 * <p>여기에는 화면 공통 관심사(예: XSS 치환, 공통 모델/코드 세팅, 접근 로깅 등)를
 * 둡니다. 현재는 예시로 접근 로그를 남기고, 이 필터가 실제로 적용되었음을 확인할 수 있도록
 * 응답에 {@code X-Web-Common-Filter} 헤더를 추가합니다.
 * (검증용이며, 운영에서는 불필요하면 제거하세요.)</p>
 */
public class WebCommonFilter extends OncePerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebCommonFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		// 화면 공통 처리 예시: 접근 로깅 + 적용 여부 확인 헤더
		LOGGER.debug("WebCommonFilter applied: {} {}", request.getMethod(), request.getRequestURI());
		response.setHeader("X-Web-Common-Filter", "applied");

		// TODO: 필요 시 XSS 치환용 요청 래퍼, 공통 코드/모델 세팅 등을 추가

		chain.doFilter(request, response);
	}
}
