package egovframework.example.cmm.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Provides API clients (HMAC credentials + allowed IPs) from the database, with
 * a small time-based cache so we don't hit the DB on every request. Credentials
 * can be created/revoked/edited in the API_CLIENT / API_CLIENT_IP tables and take
 * effect within {@link #CACHE_TTL_MILLIS}.
 */
@Service("apiClientAuthService")
public class ApiClientAuthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientAuthService.class);

	/** cache time-to-live (ms). DB changes propagate within this window. */
	public static final long CACHE_TTL_MILLIS = 60_000L;

	@Resource(name = "apiClientMapper")
	private ApiClientMapper apiClientMapper;

	private volatile Map<String, ApiClientVO> cache = new HashMap<String, ApiClientVO>();
	private final AtomicLong loadedAt = new AtomicLong(0L);

	/** returns the client for the api key, or null if unknown. */
	public ApiClientVO getClient(String apiKey) {
		if (apiKey == null) {
			return null;
		}
		long now = System.currentTimeMillis();
		if (now - loadedAt.get() > CACHE_TTL_MILLIS) {
			reload();
		}
		return cache.get(apiKey);
	}

	/** force a reload from DB (also used at first access). */
	public synchronized void reload() {
		try {
			Map<String, ApiClientVO> fresh = new HashMap<String, ApiClientVO>();
			for (ApiClientVO c : apiClientMapper.selectAllClients()) {
				c.setAllowedCidrs(new ArrayList<String>());
				fresh.put(c.getApiKey(), c);
			}
			for (Map<String, Object> row : apiClientMapper.selectAllClientIps()) {
				String key = null;
				String cidr = null;
				// column-label case varies by DB (HSQLDB folds to upper-case), so match case-insensitively
				for (Map.Entry<String, Object> e : row.entrySet()) {
					if (e.getValue() == null) {
						continue;
					}
					String col = e.getKey();
					if ("apiKey".equalsIgnoreCase(col) || "api_key".equalsIgnoreCase(col)) {
						key = String.valueOf(e.getValue());
					} else if ("cidr".equalsIgnoreCase(col)) {
						cidr = String.valueOf(e.getValue());
					}
				}
				if (key != null && cidr != null) {
					ApiClientVO c = fresh.get(key);
					if (c != null) {
						c.getAllowedCidrs().add(cidr);
					}
				}
			}
			cache = fresh;
			loadedAt.set(System.currentTimeMillis());
			LOGGER.debug("API client cache reloaded: {} client(s)", fresh.size());
		} catch (Exception e) {
			// keep serving the previous cache on transient DB errors
			LOGGER.error("Failed to reload API clients from DB; keeping previous cache", e);
			loadedAt.set(System.currentTimeMillis());
		}
	}

	/** for diagnostics */
	public List<String> knownApiKeys() {
		return new ArrayList<String>(cache.keySet());
	}
}
