package egovframework.example.cmm.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DB(API_CLIENT / API_CLIENT_IP)에서 로딩한 API 클라이언트(HMAC 자격증명) 정보.
 *
 * <p>외부 시스템 1개당 1개의 인스턴스로, API 키·비밀키·사용여부·허용 IP 목록을 담습니다.
 * {@link ApiClientAuthService} 가 DB 에서 읽어 캐시에 보관하고,
 * {@link HmacAuthFilter} 가 인증·IP 검증 시 참조합니다.</p>
 */
public class ApiClientVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/** API 키(클라이언트 식별자, 요청 헤더 X-API-KEY 값) */
	private String apiKey;

	/** 공유 비밀키(HMAC 서명 계산용). 외부에 노출되면 안 됨. */
	private String secret;

	/** 사용 여부 (Y: 사용, N: 사용 중지 → 인증 거부) */
	private String useYn;

	/** 클라이언트 표시 이름(관리용) */
	private String clientName;

	/** 허용 출발지 IP 대역(CIDR) 목록. 비어 있으면 이 클라이언트는 IP 제한 없음. */
	private List<String> allowedCidrs = new ArrayList<String>();

	/** 사용 가능한(USE_YN='Y') 클라이언트인지 여부 */
	public boolean isEnabled() {
		return "Y".equalsIgnoreCase(useYn);
	}

	/** API 키를 반환한다. */
	public String getApiKey() {
		return apiKey;
	}

	/** API 키를 설정한다. */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/** 비밀키를 반환한다. */
	public String getSecret() {
		return secret;
	}

	/** 비밀키를 설정한다. */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/** 사용 여부를 반환한다. */
	public String getUseYn() {
		return useYn;
	}

	/** 사용 여부를 설정한다. */
	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	/** 클라이언트 이름을 반환한다. */
	public String getClientName() {
		return clientName;
	}

	/** 클라이언트 이름을 설정한다. */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/** 허용 IP 대역(CIDR) 목록을 반환한다. */
	public List<String> getAllowedCidrs() {
		return allowedCidrs;
	}

	/** 허용 IP 대역(CIDR) 목록을 설정한다. */
	public void setAllowedCidrs(List<String> allowedCidrs) {
		this.allowedCidrs = allowedCidrs;
	}
}
