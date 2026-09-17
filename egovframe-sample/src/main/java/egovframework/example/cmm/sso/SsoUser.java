package egovframework.example.cmm.sso;

import java.io.Serializable;

/**
 * SSO 로 인증된 사용자(주체). 인증 성공 시 세션에 저장되어 이후 요청에서 신원 확인에 사용됩니다.
 */
public class SsoUser implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 사용자 ID */
	private final String id;

	/** 표시 이름 */
	private final String name;

	public SsoUser(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
