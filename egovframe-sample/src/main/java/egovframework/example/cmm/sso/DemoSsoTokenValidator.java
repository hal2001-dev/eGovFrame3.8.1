package egovframework.example.cmm.sso;

import org.springframework.stereotype.Service;

/**
 * 데모용 SSO 토큰 검증기 (오프라인 동작).
 *
 * <p>{@code demo-token-<userId>} 형식의 토큰을 유효한 것으로 간주하고 해당 사용자로 매핑합니다.
 * (예: {@code demo-token-alice} → id=alice) <b>실제 운영에서는 이 클래스를 SSO 서버 검증 로직으로
 * 교체하세요.</b> 필터는 {@link SsoTokenValidator} 인터페이스에만 의존하므로 교체가 쉽습니다.</p>
 */
@Service("ssoTokenValidator")
public class DemoSsoTokenValidator implements SsoTokenValidator {

	private static final String PREFIX = "demo-token-";

	@Override
	public SsoUser validate(String token) {
		if (token == null) {
			return null;
		}
		String t = token.trim();
		if (!t.startsWith(PREFIX) || t.length() <= PREFIX.length()) {
			return null;
		}
		String userId = t.substring(PREFIX.length());
		// 데모: 토큰에서 추출한 id 로 사용자 생성. 실제로는 SSO 서버가 준 사용자정보 사용.
		return new SsoUser(userId, userId + " (SSO)");
	}
}
