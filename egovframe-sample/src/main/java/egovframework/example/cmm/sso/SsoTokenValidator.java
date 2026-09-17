package egovframework.example.cmm.sso;

/**
 * SSO 토큰 검증 확장 지점(SPI).
 *
 * <p>실제 환경에서는 이 인터페이스의 구현을 SSO 제품/표준에 맞게 교체합니다.
 * 예: SAML Assertion 검증, OAuth2/OIDC 토큰 introspection, 기관 SSO 에이전트 API 호출 등.
 * 데모용 기본 구현은 {@link DemoSsoTokenValidator} 입니다.</p>
 */
public interface SsoTokenValidator {

	/**
	 * SSO 토큰을 검증하고 사용자 신원을 반환한다.
	 *
	 * @param token 클라이언트가 전달한 SSO 토큰(없을 수 있음)
	 * @return 유효하면 {@link SsoUser}, 유효하지 않으면 null
	 */
	SsoUser validate(String token);
}
