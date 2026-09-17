package egovframework.example.cmm.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 공통 JSON 유틸리티 (Jackson 기반).
 *
 * <p>기존 {@code org.json.simple.JSONObject}(=HashMap 상속) 사용부를 대체하기 위한 헬퍼입니다.
 * key/value 해시가 필요하면 {@link #toMap(String)}, 배열은 {@link #toList(String)},
 * 트리 탐색은 {@link #readTree(String)}, 고정 구조는 {@link #toObject(String, Class)} 를 씁니다.</p>
 *
 * <p>{@link ObjectMapper} 는 스레드-세이프하므로 하나만 만들어 재사용합니다. 검사 예외는
 * 편의를 위해 {@link RuntimeException} 으로 감싸 던집니다(호출부의 try/catch 부담 감소).</p>
 *
 * <pre>
 *   Map&lt;String,Object&gt; m = EgovJsonUtil.toMap(jsonString);   // {"a":1} → {a=1}
 *   String name = (String) m.get("name");
 *   String json = EgovJsonUtil.toJson(m);                         // 객체/맵 → 문자열
 *   SampleVO vo = EgovJsonUtil.toObject(jsonString, SampleVO.class);
 * </pre>
 */
public final class EgovJsonUtil {

	/** 재사용 ObjectMapper (스레드-세이프) */
	private static final ObjectMapper MAPPER = new ObjectMapper()
			// 모르는 필드가 있어도 실패하지 않도록(하위호환 안전)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private EgovJsonUtil() {
	}

	/** 필요 시 매퍼를 직접 얻어 세밀 제어 (모듈 등록 등) */
	public static ObjectMapper mapper() {
		return MAPPER;
	}

	/** 객체/맵/리스트 → JSON 문자열 */
	public static String toJson(Object value) {
		try {
			return MAPPER.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException("JSON 직렬화 오류: " + e.getMessage(), e);
		}
	}

	/** JSON 문자열 → Map&lt;String,Object&gt; (순서 유지, key/value 해시 구조) */
	public static Map<String, Object> toMap(String json) {
		if (json == null || json.trim().isEmpty()) {
			return new LinkedHashMap<String, Object>();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("JSON → Map 파싱 오류: " + e.getMessage(), e);
		}
	}

	/** JSON 배열 문자열 → List&lt;Object&gt; */
	public static List<Object> toList(String json) {
		try {
			return MAPPER.readValue(json, new TypeReference<List<Object>>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("JSON → List 파싱 오류: " + e.getMessage(), e);
		}
	}

	/** JSON 문자열 → 지정 타입 POJO */
	public static <T> T toObject(String json, Class<T> type) {
		try {
			return MAPPER.readValue(json, type);
		} catch (Exception e) {
			throw new RuntimeException("JSON → " + type.getSimpleName() + " 파싱 오류: " + e.getMessage(), e);
		}
	}

	/** JSON 문자열 → 제네릭 타입 (예: List&lt;SampleVO&gt;) */
	public static <T> T toObject(String json, TypeReference<T> typeRef) {
		try {
			return MAPPER.readValue(json, typeRef);
		} catch (Exception e) {
			throw new RuntimeException("JSON 파싱 오류: " + e.getMessage(), e);
		}
	}

	/** JSON 문자열 → 트리(JsonNode). 중첩/선택 접근에 안전(path 체이닝) */
	public static JsonNode readTree(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (Exception e) {
			throw new RuntimeException("JSON 트리 파싱 오류: " + e.getMessage(), e);
		}
	}
}
