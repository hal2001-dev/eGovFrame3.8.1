package egovframework.example.cmm.sso.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import egovframework.example.cmm.sso.SsoAuthFilter;
import egovframework.example.cmm.sso.SsoUser;

/**
 * SSO 로그인/로그아웃 컨트롤러 (데모).
 *
 * <p>실제 환경에서는 이 로그인 화면 대신 <b>IdP(SSO 서버)로 리다이렉트</b>되고, IdP 인증 후
 * 콜백에서 토큰을 검증합니다. 여기서는 오프라인 데모를 위해 간단한 로그인 폼으로 세션을 만듭니다.
 * {@code /sso/*} 는 SSO 필터 매핑(/secure/*) 밖이라 인증 없이 접근됩니다.</p>
 */
@Controller
@RequestMapping("/sso")
public class SsoLoginController {

	/** 로그인 화면 (returnUrl 유지) */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String loginForm(@RequestParam(value = "returnUrl", required = false, defaultValue = "/secure/home.do") String returnUrl,
			Model model) {
		model.addAttribute("returnUrl", returnUrl);
		return "sso/login";
	}

	/** 로그인 처리(데모): 세션에 사용자 저장 후 returnUrl 로 이동 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(@RequestParam("username") String username,
			@RequestParam(value = "returnUrl", required = false, defaultValue = "/secure/home.do") String returnUrl,
			HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.setAttribute(SsoAuthFilter.SESSION_USER, new SsoUser(username, username + " (SSO)"));
		// 오픈 리다이렉트 방지: 컨텍스트 내부 경로만 허용
		if (returnUrl == null || !returnUrl.startsWith("/") || returnUrl.startsWith("//")) {
			returnUrl = "/secure/home.do";
		}
		return "redirect:" + returnUrl;
	}

	/** 로그아웃: 세션 무효화 후 로그인 화면으로 */
	@RequestMapping(value = "/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/sso/login";
	}
}
