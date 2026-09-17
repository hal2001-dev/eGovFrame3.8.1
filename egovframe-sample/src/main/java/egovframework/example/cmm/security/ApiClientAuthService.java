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
 * API 클라이언트(HMAC 자격증명 + 허용 IP)를 DB 에서 제공하는 서비스.
 *
 * <p>매 요청마다 DB 를 조회하면 부하가 크므로, 짧은 <b>시간 기반 캐시</b>를 둡니다.
 * 캐시 만료({@link #CACHE_TTL_MILLIS}) 이후 첫 접근 시 DB 에서 전체를 다시 읽어옵니다.
 * 따라서 API_CLIENT / API_CLIENT_IP 테이블에서 키를 발급·폐기(USE_YN)·IP 변경하면
 * <b>재배포 없이</b> 최대 TTL(기본 60초) 안에 반영됩니다.</p>
 *
 * <p>{@link HmacAuthFilter} 는 서블릿 필터라 스프링이 주입해주지 않으므로,
 * 요청 처리 시점에 웹 애플리케이션 컨텍스트에서 이 서비스 빈을 찾아 사용합니다.</p>
 */
@Service("apiClientAuthService")
public class ApiClientAuthService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientAuthService.class);

	/** 캐시 유효 시간(밀리초). 이 시간이 지나면 다음 접근 때 DB 에서 재로딩한다. */
	public static final long CACHE_TTL_MILLIS = 60_000L;

	/** API 클라이언트 조회 매퍼 */
	@Resource(name = "apiClientMapper")
	private ApiClientMapper apiClientMapper;

	/** apiKey → 클라이언트 정보 캐시. 통째로 교체(volatile)해 읽기 시 락 없이 안전하게 참조. */
	private volatile Map<String, ApiClientVO> cache = new HashMap<String, ApiClientVO>();

	/** 마지막 로딩 시각(밀리초). TTL 만료 판단용. */
	private final AtomicLong loadedAt = new AtomicLong(0L);

	/**
	 * API 키에 해당하는 클라이언트를 반환한다. 없으면 null.
	 * 캐시가 만료되었으면 먼저 DB 에서 재로딩한다.
	 */
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

	/**
	 * DB 에서 전체 클라이언트와 허용 IP 를 읽어 캐시를 새로 구성한다.
	 * (최초 접근 시에도 호출됨) DB 오류 시에는 예외를 삼키고 <b>기존 캐시를 유지</b>해
	 * 일시적 장애로 전체 인증이 중단되지 않도록 한다.
	 */
	public synchronized void reload() {
		try {
			Map<String, ApiClientVO> fresh = new HashMap<String, ApiClientVO>();
			// 1) 클라이언트 기본정보 로딩
			for (ApiClientVO c : apiClientMapper.selectAllClients()) {
				c.setAllowedCidrs(new ArrayList<String>());
				fresh.put(c.getApiKey(), c);
			}
			// 2) 허용 IP 행을 각 클라이언트에 매핑
			for (Map<String, Object> row : apiClientMapper.selectAllClientIps()) {
				String key = null;
				String cidr = null;
				// DB 별로 컬럼 라벨 대소문자가 다를 수 있어(HSQLDB 는 대문자) 대소문자 무시로 읽는다
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
			// DB 일시 장애 시 이전 캐시를 그대로 사용(다음 TTL 후 재시도)
			LOGGER.error("Failed to reload API clients from DB; keeping previous cache", e);
			loadedAt.set(System.currentTimeMillis());
		}
	}

	/** 진단용: 현재 캐시에 로딩된 API 키 목록 */
	public List<String> knownApiKeys() {
		return new ArrayList<String>(cache.keySet());
	}
}
