package egovframework.example.cmm.security;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * Loads API clients (HMAC credentials) and their allowed IP ranges from the DB.
 */
@Mapper
public interface ApiClientMapper {

	/** all clients (both enabled and disabled) */
	List<ApiClientVO> selectAllClients();

	/** all (apiKey, cidr) rows; assembled onto clients in the service */
	List<Map<String, Object>> selectAllClientIps();
}
