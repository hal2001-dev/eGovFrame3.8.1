package egovframework.example.sample.api;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import egovframework.example.sample.service.SampleVO;

/**
 * 샘플(게시판) REST API 컨트롤러. 요청/응답을 JSON 으로 처리한다.
 *
 * <p>서버-투-서버(외부 시스템 연계)용 엔드포인트로, {@code /api/*} 경로는 별도의
 * HMAC 인증 필터({@code egovframework.example.cmm.security.HmacAuthFilter})로 보호됩니다.
 * 따라서 이 컨트롤러 자체에는 인증 코드가 없고 순수 CRUD 로직만 담습니다.</p>
 *
 * <p>{@code @RestController} 는 모든 메서드 반환값을 (뷰가 아니라) 응답 본문으로
 * 직렬화하며, Jackson 이 객체 ↔ JSON 변환을 담당합니다.</p>
 *
 * <pre>
 *  GET    /api/samples            목록 조회 (?searchCondition=&amp;searchKeyword=)
 *  GET    /api/samples/{id}       단건 조회
 *  POST   /api/samples            생성
 *  PUT    /api/samples/{id}       수정
 *  DELETE /api/samples/{id}       삭제
 * </pre>
 */
@RestController
@RequestMapping("/api/samples")
public class EgovSampleApiController {

	/** 샘플 업무 서비스 (화면 컨트롤러와 동일한 서비스 재사용) */
	@Resource(name = "egovSampleService")
	private EgovSampleService egovSampleService;

	/**
	 * 목록 조회. 검색어가 있으면 이름/설명으로 LIKE 검색한다.
	 *
	 * @param searchCondition 검색 구분(0:이름, 1:설명, 그 외:전체)
	 * @param searchKeyword   검색어
	 * @return {@code {"totalCount": 총건수, "list": [ ... ]}} 형태의 JSON
	 */
	@GetMapping
	public Map<String, Object> list(
			@RequestParam(value = "searchCondition", required = false, defaultValue = "") String searchCondition,
			@RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword)
			throws Exception {
		SampleDefaultVO searchVO = new SampleDefaultVO();
		searchVO.setSearchCondition(searchCondition);
		searchVO.setSearchKeyword(searchKeyword);

		List<SampleVO> list = egovSampleService.selectSampleList(searchVO);
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("totalCount", egovSampleService.selectSampleListTotCnt(searchVO));
		body.put("list", list);
		return body;
	}

	/**
	 * 단건 조회. 존재하지 않으면 서비스가 예외를 던지고,
	 * {@link #handleNotFound} 가 404 로 변환한다.
	 */
	@GetMapping("/{id}")
	public SampleVO get(@PathVariable("id") String id) throws Exception {
		return egovSampleService.selectSample(id);
	}

	/**
	 * 생성. 생성된 리소스를 본문으로, Location 헤더에 상세 URL 을 담아 201 로 응답한다.
	 *
	 * @param sampleVO 요청 본문(JSON)에서 바인딩된 게시글
	 */
	@PostMapping
	public ResponseEntity<SampleVO> create(@RequestBody SampleVO sampleVO) throws Exception {
		String id = egovSampleService.insertSample(sampleVO);
		SampleVO created = egovSampleService.selectSample(id);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(id).toUri();
		return ResponseEntity.created(location).body(created);
	}

	/**
	 * 수정. 먼저 존재 여부를 확인(없으면 404)하고, 경로의 ID 를 본문에 강제로 세팅해
	 * 경로-본문 ID 불일치를 방지한다.
	 */
	@PutMapping("/{id}")
	public SampleVO update(@PathVariable("id") String id, @RequestBody SampleVO sampleVO) throws Exception {
		egovSampleService.selectSample(id); // 없으면 404
		sampleVO.setId(id);
		egovSampleService.updateSample(sampleVO);
		return egovSampleService.selectSample(id);
	}

	/** 삭제. 존재 여부 확인 후 삭제하고 204(No Content)로 응답한다. */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") String id) throws Exception {
		egovSampleService.selectSample(id); // 없으면 404
		egovSampleService.deleteSample(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 존재하지 않는 ID 접근 시 발생하는 예외를 404 JSON 응답으로 변환한다.
	 * (서비스가 {@link IllegalArgumentException} 을 던진다.)
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleNotFound(IllegalArgumentException e) {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("status", 404);
		body.put("message", e.getMessage());
		return new ResponseEntity<Map<String, Object>>(body, HttpStatus.NOT_FOUND);
	}
}
