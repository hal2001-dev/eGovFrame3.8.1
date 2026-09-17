package egovframework.example.cmm.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An API client (HMAC credentials) loaded from the database.
 */
public class ApiClientVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String apiKey;
	private String secret;
	private String useYn;
	private String clientName;

	/** allowed source IP ranges (CIDR); empty = no IP restriction for this client */
	private List<String> allowedCidrs = new ArrayList<String>();

	public boolean isEnabled() {
		return "Y".equalsIgnoreCase(useYn);
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getUseYn() {
		return useYn;
	}

	public void setUseYn(String useYn) {
		this.useYn = useYn;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public List<String> getAllowedCidrs() {
		return allowedCidrs;
	}

	public void setAllowedCidrs(List<String> allowedCidrs) {
		this.allowedCidrs = allowedCidrs;
	}
}
