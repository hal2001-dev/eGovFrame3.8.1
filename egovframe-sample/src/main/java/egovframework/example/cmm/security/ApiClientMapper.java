package egovframework.example.cmm.security;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * API 클라이언트(HMAC 자격증명)와 허용 IP 를 DB 에서 조회하는 MyBatis 매퍼.
 *
 * <p>매퍼 XML 은 egovframework/mapper/example/api_client_SQL_hsql.xml 이며,
 * {@link Mapper} 애노테이션으로 MapperScannerConfigurer 에 의해 "apiClientMapper"
 * 빈으로 자동 등록됩니다. 개별 클라이언트를 매번 조회하지 않고, 서비스
 * ({@link ApiClientAuthService})가 전체를 한 번에 읽어 캐시하는 방식이라
 * 여기서는 "전체 조회" 메서드만 제공합니다.</p>
 */
@Mapper
public interface ApiClientMapper {

	/** 전체 클라이언트 목록을 조회한다. (사용/미사용 모두 포함) */
	List<ApiClientVO> selectAllClients();

	/**
	 * 전체 (apiKey, cidr) 행을 조회한다. 서비스에서 각 클라이언트에 매핑해 붙인다.
	 *
	 * <p>반환 타입이 Map 인 이유: DB 마다 컬럼 라벨 대소문자가 다를 수 있어
	 * (HSQLDB 는 대문자로 접음) 서비스에서 대소문자 무시로 키를 읽어 처리한다.</p>
	 */
	List<Map<String, Object>> selectAllClientIps();
}
