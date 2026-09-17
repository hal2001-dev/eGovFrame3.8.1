package egovframework.example.cmm.sso.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import egovframework.example.cmm.sso.SsoAuthFilter;
import egovframework.example.cmm.sso.SsoUser;

/**
 * SSO 로 보호되는 데모 영역({@code /secure/*}).
 *
 * <p>이 경로는 {@link SsoAuthFilter} 가 가로채므로, 인증되지 않으면 컨트롤러에 도달하기 전에
 * 로그인으로 리다이렉트됩니다. 따라서 여기서는 세션에 사용자가 반드시 있다고 가정할 수 있습니다.</p>
 */
@Controller
@RequestMapping("/secure")
public class SecureController {

	/** 인증된 사용자 정보를 JSON 으로 반환(보호 영역 진입 확인용) */
	@GetMapping("/home.do")
	@ResponseBody
	public Map<String, Object> home(HttpServletRequest request) {
		SsoUser user = (SsoUser) request.getSession().getAttribute(SsoAuthFilter.SESSION_USER);
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("message", "SSO 보호 영역 진입 성공");
		body.put("userId", user != null ? user.getId() : null);
		body.put("userName", user != null ? user.getName() : null);
		return body;
	}
}
